(ns clojure-ring-reitit-datahike-realworld-example-app.validations
  (:require [struct.core :as st]))

;; TODO add custom error messages
(def login-user-schema [[:email st/required st/email]
                        [:password st/required [st/min-count 8] [st/max-count 100]]])

(defn validate-body [body schema]
  (when-let [errors (first (st/validate body schema))]
    (->> errors
         (map (fn [[k v]] [k [v]]))
         (into {}))))

(comment

  (st/validate {:email "1" :password "342323"} login-user-schema)
  ;; => [nil {:email "1@email.com", :password "12321342323"}]

  (st/validate {:email "1@emailcom" :password "12321342323"} login-user-schema)
  ;; => [{:email "must be a valid email"} {:password "12321342323"}]

  (st/validate {:email "1@emailcom" :password "2323"} login-user-schema)
  ;; => [{:email "must be a valid email", :password "less than the minimum 8"} {}]

  (validate-body {:email "1@emailcom" :password "2323"} login-user-schema)
  ;; => {:email ["must be a valid email"], :password ["less than the minimum 8"]}

  (validate-body {:email "1@email.com" :password "2323123213"} login-user-schema)
  ;; => nil

  (map (fn [[k v]] [k [v]]) {:email "must be a valid email", :password "less than the minimum 8"})
  ;; => ([:email ["must be a valid email"]] [:password ["less than the minimum 8"]])

  )
