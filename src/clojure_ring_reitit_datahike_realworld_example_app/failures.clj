(ns clojure-ring-reitit-datahike-realworld-example-app.failures
  (:require [clojure.string :refer [join]]
            [failjure.core :as f]))

(defrecord ValidationFailure [message errors]
  f/HasFailed
  (failed? [_] true)
  (message [this] (:message this)))

(defn invalid-body [errors]
  (->ValidationFailure (str "Validation failure - " (join ", " (map (fn [[k v]] (str (name k) " " v)) errors)))
                       errors))

(defrecord ApiFailure [message error-type]
  f/HasFailed
  (failed? [_] true)
  (message [this] (:message this)))

(defn api-fail [message error-type]
  (->ApiFailure (str message " - " (name error-type)) error-type))

(def email-and-username-exist-fail (api-fail "Email and username already exist" :email-and-username-exist))
(def email-exists-fail (api-fail "Email already exists" :email-exists))
(def username-exists-fail (api-fail "Username already exists" :username-exists))
(def password-invalid-fail (api-fail "User's password didn't match" :email-or-password-invalid))
(def email-invalid-fail (api-fail "User with email not found" :email-or-password-invalid))
(def user-not-found-fail (api-fail "User with id not found" :user-not-found))
