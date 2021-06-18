(ns clojure-ring-reitit-datahike-realworld-example-app.validations
  (:require [clojure.string :as s]
            [struct.core :as st]))

;; use intermedium coerce step to sanitize input
(def login-user-schema [[:email
                         [st/required :message "can't be blank"]
                         {:coerce s/trim}
                         [st/email :message "is invalid"]]
                        [:password
                         [st/required :message "can't be blank"]
                         {:coerce s/trim}
                         [st/min-count 8 :message "is too short (minimum is %s character)"]
                         [st/max-count 100 :message "is too long (maximum is %s character)"]]])

(defn validate-body
  "If valid then returns nil and sanitized body otherwise list of errors and partial body"
  [body schema]
  (st/validate body schema {:strip true}))

(comment

  (st/validate {:password "342323"} login-user-schema)
;; => [{:email "can't be blank", :password "is too short (minimum is 8 character)"} {}]

  (st/validate {:email nil :password "342323"} login-user-schema)
;; => [{:email "can't be blank", :password "is too short (minimum is 8 character)"} {}]

  (st/validate {:email "" :password "342323"} login-user-schema)
;; => [{:email "can't be blank", :password "is too short (minimum is 8 character)"} {}]

  (st/validate {:email "1" :password "342323"} login-user-schema)
;; => [{:email "is invalid", :password "is too short (minimum is 8 character)"} {}]

  (st/validate {:email "1@emailcom" :password "12321342323"} login-user-schema)
;; => [{:email "is invalid"} {:password "12321342323"}]

  (st/validate {:email "1@emailcom" :password "2323"} login-user-schema)
;; => [{:email "is invalid", :password "is too short (minimum is 8 character)"} {}]

  (st/validate {:email "s123@email.com" :password "232323343"} login-user-schema)
;; => [nil {:email "s123@email.com", :password "232323343"}]

  (st/validate {:str " 1 2 3 4 5 " :foo "sdfdsf"} [[:str st/required {:coerce s/trim} st/string [st/min-count 5]]] {:strip true})
;; => [nil {:str "1 2 3 4 5"}]

  (validate-body {:email "1@emailcom" :password "23233"} login-user-schema)
;; => [{:email "is invalid", :password "is too short (minimum is 8 character)"} {}]

  (validate-body {:email "1@email.com" :password "2323123213"} login-user-schema)
;; => [nil {:password "2323123213", :email "1@email.com"}]

  )
