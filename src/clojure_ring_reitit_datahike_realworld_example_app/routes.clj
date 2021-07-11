(ns clojure-ring-reitit-datahike-realworld-example-app.routes
  (:require [clojure-ring-reitit-datahike-realworld-example-app.handlers :as h]
            [clojure-ring-reitit-datahike-realworld-example-app.middlewares :refer [auth-middleware]]))

(defn user-routes [token-auth-middleware]
  [["/users/login" {:post {:handler h/login-user
                           :parameters {:body :clojure-ring-reitit-datahike-realworld-example-app.specs/login-user-request}
                           :responses {200 {:body :clojure-ring-reitit-datahike-realworld-example-app.specs/login-user-response}}}}]

   ["/users" {:post {:handler h/register-user
                     :parameters {:body :clojure-ring-reitit-datahike-realworld-example-app.specs/register-user-request}
                     :responses {200 {:body :clojure-ring-reitit-datahike-realworld-example-app.specs/register-user-response}}}}]

   ["/user" {:get {:handler h/get-user
                   :responses {200 {:body :clojure-ring-reitit-datahike-realworld-example-app.specs/get-user-response}}}
             :put {:handler h/update-user
                   :parameters {:body :clojure-ring-reitit-datahike-realworld-example-app.specs/update-user-request}
                   :responses {200 {:body :clojure-ring-reitit-datahike-realworld-example-app.specs/update-user-response}}}
             :middleware [token-auth-middleware auth-middleware]}]])
