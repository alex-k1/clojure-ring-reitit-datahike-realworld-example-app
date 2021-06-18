(ns clojure-ring-reitit-datahike-realworld-example-app.handlers
  (:require [clojure-ring-reitit-datahike-realworld-example-app.validations :as v]
            [ring.util.http-response :refer [ok unprocessable-entity]]))

(defn- format-errors [errors]
  (->> errors
       (map (fn [[field message]] [field [message]]))
       (into {})))

(defn login-user [request]
  (let [body (get-in request [:parameters :body])
        login-user-svc (get-in request [:services :login-user-svc])
        [errors valid-body] (v/validate-body body v/login-user-schema)]
    (if (nil? errors)
      (if-let [user (login-user-svc valid-body)]
        (ok {:user user})
        (unprocessable-entity {:errors {"email or password" ["is invalid"]}}))
      (unprocessable-entity {:errors (format-errors errors)}))))

(defn register-user [request])

(defn get-user [request])

(defn update-user [request])

(comment

  (login-user {:parameters {:body {:email "123asd.com" :password "6900"}}})
;; => {:status 422, :headers {}, :body {:errors {:email ["is invalid"], :password ["is too short (minimum is 8 character)"]}}}

  (require '[clojure.spec.alpha :as s]
           '[clojure.spec.gen.alpha :as gen])

  (login-user {:parameters {:body {:email "123@asd.com" :password "124556900"}}
               :services {:login-user-svc (constantly (gen/generate (s/gen :clojure-ring-reitit-datahike-realworld-example-app.specs/user)))}})
;; => {:status 200, :headers {}, :body {:user {:email "nIUoi", :token "Z36o74FF2u3", :username "165PQNSOw1lTKU0R6e9K3uF8Cy", :bio "DH400me2gW3", :image "x69492F34"}}}

  (login-user {:parameters {:body {:email "123@asd.com" :password "124556900"}}
               :services {:login-user-svc (constantly nil)}})
;; => {:status 422, :headers {}, :body {:errors {"email or password" ["is invalid"]}}}

  )
