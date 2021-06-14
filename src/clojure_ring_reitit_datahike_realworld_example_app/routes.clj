(ns clojure-ring-reitit-datahike-realworld-example-app.routes
  (:require [clojure-ring-reitit-datahike-realworld-example-app.handlers :as h]))

(def user-routes [["/users/login" {:post {:handler h/login-user
                                          :parameters {:body ::login-user-request}
                                          :responses {200 {:body ::login-user-response}}}}]
                  ["/users" {:post {:handler h/register-user
                                    :parameters {:body ::register-user-request}
                                    :responses {200 {:body ::register-user-response}}}}]
                  ["/user" {:get {:handler h/get-user
                                  :responses {200 {:body ::get-user-response}}}
                            :put {:handler h/update-user
                                  :parameters {:body ::update-user-request}
                                  :responses {200 {:body ::update-user-response}}}}]])
