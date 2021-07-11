(ns clojure-ring-reitit-datahike-realworld-example-app.core
  (:require [clojure-ring-reitit-datahike-realworld-example-app.apis :as apis]
            [clojure-ring-reitit-datahike-realworld-example-app.db :as db]
            [clojure-ring-reitit-datahike-realworld-example-app.middlewares :as middlewares]
            [clojure-ring-reitit-datahike-realworld-example-app.routes :as routes]
            [clojure-ring-reitit-datahike-realworld-example-app.token :as token]
            [datahike.api :as d]
            [integrant.core :as ig]
            [muuntaja.core :as m]
            [reitit.coercion.spec :as rcs]
            [reitit.ring :as rr]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as rrm]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn app [routes apis]
  (rr/ring-handler
   (rr/router [["/api" routes]]
              {:data {:muuntaja m/instance
                      :coercion rcs/coercion
                      :middleware [middlewares/apis-middleware
                                   rrm/format-middleware
                                   rrc/coerce-exceptions-middleware
                                   rrc/coerce-request-middleware
                                   rrc/coerce-response-middleware]
                      :apis apis}})
   (rr/create-default-handler)))

;; TODO externilize to file
(def config
  {:adapter/jetty {:handler (ig/ref :handler/app) :port 3000 :join? false}
   :handler/app {:routes (ig/ref :routes/all-routes) :apis (ig/ref :apis/all-apis)}
   :routes/all-routes {:token-auth-middleware (ig/ref :middleware/token-auth)}
   :middleware/token-auth {}
   :apis/all-apis {:conn (ig/ref :db/conn)}
   :db/conn {:store {:backend :mem :id "mem-db"}
             :initial-tx db/schema}})

(defmethod ig/init-key :adapter/jetty [_ {:keys [handler] :as opts}]
  (run-jetty handler (dissoc opts :handler)))

(defmethod ig/halt-key! :adapter/jetty [_ ^org.eclipse.jetty.server.Server server]
  (.stop server))

(defmethod ig/init-key :handler/app [_ {:keys [routes apis]}]
  (app routes apis))

(defmethod ig/init-key :routes/all-routes [_ {:keys [token-auth-middleware]}]
  (routes/user-routes token-auth-middleware))

;; TODO externilize
(def jwt-key "key")
(def jwt-expiration-minutes 60)

(defmethod ig/init-key :middleware/token-auth [_ _]
  (middlewares/token-auth-middleware (partial token/decode jwt-key)))

(defmethod ig/init-key :apis/all-apis [_ {:keys [conn]}]
  (let [generate-token (partial token/encode jwt-key jwt-expiration-minutes)]
    {:login-user (partial apis/login-user {:find-user-by-email (partial db/find-user-by-email conn)
                                           :generate-token generate-token})

     :register-user (partial apis/register-user {:find-user-by-email (partial db/find-user-by-email conn)
                                                 :find-user-by-username (partial db/find-user-by-username conn)
                                                 :add-user (partial db/add-user conn)
                                                 :generate-token generate-token})

     :get-user (partial apis/get-user {:find-user-by-user-id (partial db/find-user-by-user-id conn)
                                       :generate-token generate-token})

     :update-user (partial apis/update-user {:find-user-by-email (partial db/find-user-by-email conn)
                                             :find-user-by-username (partial db/find-user-by-username conn)
                                             :update-user (partial db/update-user conn)
                                             :generate-token generate-token})}))

(defmethod ig/init-key :db/conn [_ cfg]
  (when-not (d/database-exists? cfg)
    (d/create-database cfg))
  (d/connect cfg))

(defmethod ig/halt-key! :db/conn [_ conn]
  (d/release conn))

(comment
  (def conn (ig/init-key :db/conn {:store {:backend :mem :id "mem-db"}}))

  (d/delete-database {:store {:backend :mem :id "mem-db"}})

  (def user-id 1)
  (d/q '[:find ?n .
         :in $ ?user-id
         :where
         [?e :user-name ?n]
         [?e :user-id ?user-id]] @conn user-id)

  (require '[ring.mock.request :as mock]
           '[buddy.auth :refer [authenticated?]]
           '[buddy.auth.backends :as backends]
           '[buddy.auth.middleware :refer [wrap-authentication]]
           '[ring.util.http-response :refer [unauthorized]])

  (def my-g-mw
    {:name ::my-g-mw
     :compile (fn [{:keys [apis]} _]
                (fn [handler]
                  (fn [rq]
                    (handler (assoc rq :apis apis)))))})

  (defn my-mw [handler api-id]
    (fn [rq] (handler (assoc rq :api-id api-id))))

  (defn my-authfn [request token]
    (when (= token "abc") "user-1"))

  (defn my-token-auth-mw [handler]
    (wrap-authentication handler (backends/token {:authfn my-authfn})))

  (defn my-auth-mw [handler]
    (fn [request]
      (if (authenticated? request)
        (handler request)
        (unauthorized))))

  (def app
    (rr/ring-handler
     (rr/router [["/foo" {:get {:handler (fn [{:keys [api-id apis]}] (str api-id " " apis))}
                          :middleware [[my-mw :foo] my-token-auth-mw my-auth-mw]}]
                 ["/bar" {:get {:handler (fn [{:keys [api-id apis]}] (str api-id " " apis))}
                          :middleware [[my-mw :bar]]}]]
                {:data {:middleware [my-g-mw]
                        :apis {:foo [:api1 :api2]
                               :bar [:api3 :api4]}}})))

  (app (-> (mock/request :get "/foo")
           (mock/header "Authorization" "Token abc")))

  (app (mock/request :get "/bar"))

  )
