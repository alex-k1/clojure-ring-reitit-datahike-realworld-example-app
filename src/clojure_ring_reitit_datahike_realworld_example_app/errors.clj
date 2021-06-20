(ns clojure-ring-reitit-datahike-realworld-example-app.errors
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
  (->ApiFailure (str message" - " (name error-type)) error-type))

(defprotocol FormatFailure
  (format-failure [this]))

(extend-protocol FormatFailure
  ValidationFailure
  (format-failure [this] (->> this
                              :errors
                              (map (fn [[field message]] [field [message]]))
                              (into {})))

  ApiFailure
  (format-failure [this] (case (:error-type this)
                           :boo-error {:boo ["boo error"]}
                           {:default ["default error"]})))
