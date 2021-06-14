(ns clojure-ring-reitit-datahike-realworld-example-app.handlers
  (:require [clojure-ring-reitit-datahike-realworld-example-app.validations :as v]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [ring.util.http-response :refer [ok unprocessable-entity]]))

(defn login-user-service [conn user]
  (gen/generate (s/gen :clojure-ring-reitit-datahike-realworld-example-app.specs/login-user-response))
  nil)

;; TODO clean body
(defn login-user [request]
  (let [body (get-in request [:parameters :body])]
    (if-let [errors (v/validate-body body v/login-user-schema)]
      (unprocessable-entity {:errors errors})
      (if-let [user (login-user-service (:conn request) body)]
        (ok {:user user})
        (unprocessable-entity {:errors {:body {"email or password" ["is invalid"]}}})))))

;; {"errors":
;;  {"email": ["has already been taken"],
;;   "password":["is too short (minimum is 8 characters)"],
;;   "username":["has already been taken"]}}

(defn register-user [request])

(defn get-user [request])

(defn update-user [request])

(comment

  (login-user {:parameters {:body {:email "123asd.com" :password "6900"}}})
  ;; => {:status 422, :headers {}, :body {:errors {:email ["must be a valid email"], :password ["less than the minimum 8"]}}}

  (login-user {:parameters {:body {:email "123@asd.com" :password "124556900"}}})
  ;; => {:status 200, :headers {}, :body {:user {:user {:email "8TPN013aS0lWg39J80ju4iF6", :token "1V", :username "36W2Cx3KwmefII3L", :bio nil, :image "L3JYp76AmJ1KTt"}}}}

  )
