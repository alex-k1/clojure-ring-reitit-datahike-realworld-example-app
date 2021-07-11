(ns clojure-ring-reitit-datahike-realworld-example-app.middlewares
  (:require [buddy.auth :refer [authenticated?]]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [ring.util.http-response :refer [unauthorized]]))

(def apis-middleware
  {:name ::apis-middleware
   :compile (fn [{:keys [apis]} _]
              (fn [handler]
                (fn [request]
                  (handler (assoc request :apis apis)))))})

(defn token-auth-middleware [validate-token]
  (fn [handler]
    (wrap-authentication handler (backends/token {:authfn (fn [_ token] (validate-token token))}))))

(def auth-middleware
  (fn [handler]
    (fn [request]
      (if (authenticated? request)
        (handler request)
        (unauthorized)))))
