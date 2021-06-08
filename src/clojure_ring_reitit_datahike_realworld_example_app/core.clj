(ns clojure-ring-reitit-datahike-realworld-example-app.core
  (:require [clojure.spec.alpha :as s]
            [integrant.core :as ig]
            [muuntaja.core :as m]
            [reitit.coercion.spec :as rcs]
            [reitit.ring :as rr]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as rrm]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.http-response :refer [ok]]
            [datahike.api :as d]))

(def id (atom 0))

;; handlers
(defn health-check-handler [_]
  (ok "I am alive"))

(defn get-foo-handler [conn request]
  (let [user-id (get-in request [:parameters :path :user-id])]
    (ok {:hello (d/q '[:find ?n .
                       :in $ ?user-id
                       :where
                       [?e :user-name ?n]
                       [?e :user-id ?user-id]] @conn user-id)})))

(defn post-foo-handler [conn request]
  (let [user-id (swap! id inc)
        user-name (get-in request [:parameters :body :user-name])]
    (d/transact conn [{:user-id user-id :user-name user-name}])
    (ok {:hello (str user-id " " user-name)})))

;; schema
(s/def ::user-id integer?)
(s/def ::user-name string?)
(s/def ::request (s/keys :req-un [::user-name]))

;; routing
(defn make-app [conn]
  (rr/ring-handler
   (rr/router ["/api"
               ["/health-check" {:get {:handler health-check-handler}}]
               ["/foo/:user-id" {:get {:handler (partial get-foo-handler conn)
                                       :parameters {:path {:user-id int?}}}
                                 :post {:handler (partial post-foo-handler conn)
                                        :parameters {:body ::request}}}]]
              {:data {:muuntaja m/instance
                      :coercion rcs/coercion
                      :middleware [rrm/format-middleware
                                   rrc/coerce-exceptions-middleware
                                   rrc/coerce-request-middleware
                                   rrc/coerce-response-middleware]
                      :conn conn}})
   (rr/create-default-handler)))

;; system bootstrap
(def config
  {:adapter/jetty {:handler (ig/ref :handler/app) :port 3000 :join? false}
   :handler/app {:conn (ig/ref :db/conn)}
   :db/conn {:store {:backend :mem :id "mem-db"}
             :initial-tx [{:db/ident :user-id
                           :db/valueType :db.type/long
                           :db/cardinality :db.cardinality/one}
                          {:db/ident :user-name
                           :db/valueType :db.type/string
                           :db/cardinality :db.cardinality/one}
                          {:user-id -1 :user-name "j0hn"}]}})

(defmethod ig/init-key :adapter/jetty [_ {:keys [handler] :as opts}]
  (run-jetty handler (dissoc opts :handler)))

(defmethod ig/halt-key! :adapter/jetty [_ server]
  (.stop server))

(defmethod ig/init-key :handler/app [_ {:keys [conn]}]
  (make-app conn))

(defmethod ig/init-key :db/conn [_ cfg]
  (when-not (d/database-exists? cfg)
    (d/create-database cfg))
  (d/connect cfg))

(defmethod ig/halt-key! :db/conn [_ conn]
  (d/release conn))

(comment
  (def conn (ig/init-key :db/conn {:store {:backend :mem :id "mem-db"}}))

  (def user-id 1)
  (d/q '[:find ?n .
         :in $ ?user-id
         :where
         [?e :user-name ?n]
         [?e :user-id ?user-id]] @conn user-id)

  )
