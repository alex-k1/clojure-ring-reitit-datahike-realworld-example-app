(ns clojure-ring-reitit-datahike-realworld-example-app.validations
  (:require [clojure-ring-reitit-datahike-realworld-example-app.errors :refer [invalid-body]]
            [clojure.string :refer [trim]]
            [struct.core :as st]))

;; use intermedium coerce step to sanitize input
(def trim-coerce {:coerce (fnil trim "")})

(def login-user-schema [[:email
                         trim-coerce
                         [st/required :message "can't be blank"]
                         [st/email :message "is invalid"]]
                        [:password
                         trim-coerce
                         [st/required :message "can't be blank"]]])

(def register-user-schema [[:username
                            trim-coerce
                            [st/required :message "can't be blank"]]
                           [:email
                            trim-coerce
                            [st/required :message "can't be blank"]
                            [st/email :message "is invalid"]]
                           [:password
                            trim-coerce
                            [st/required :message "can't be blank"]
                            [st/min-count 8 :message "is too short (minimum is %s character)"]
                            [st/max-count 100 :message "is too long (maximum is %s character)"]]])

(defn validate-body
  "If valid then returns sanitized body"
  [body schema]
  (let [[errors body] (st/validate body schema {:strip true})]
    (if (nil? errors)
      body
      (invalid-body errors))))

(comment

  (st/validate {:password "342323"} login-user-schema)

  (st/validate {:email nil :password "342323"} login-user-schema)

  (st/validate {:email "" :password "342323"} login-user-schema)

  (st/validate {:email "   " :password "342323"} login-user-schema)

  (st/validate {:email "   1@emailcom" :password "12321342323"} login-user-schema)

  (st/validate {:email "1@emailcom" :password "2323"} login-user-schema)

  (st/validate {:email "1@emailcom" :password "2323"} register-user-schema)

  (st/validate {:email "s123@email.com    " :password "    232323343"} register-user-schema)

  (validate-body {:email "1@emailcom" :password "23233"} login-user-schema)

  (validate-body {:email "1@email.com" :password "2323123213"} login-user-schema)

  )
