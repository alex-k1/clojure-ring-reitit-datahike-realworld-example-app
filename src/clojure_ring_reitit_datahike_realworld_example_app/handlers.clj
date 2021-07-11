(ns clojure-ring-reitit-datahike-realworld-example-app.handlers
  (:require [clojure-ring-reitit-datahike-realworld-example-app.validations :as v]
            [failjure.core :as f]
            [ring.util.http-response :refer [internal-server-error not-found ok unprocessable-entity]])
  (:import [clojure_ring_reitit_datahike_realworld_example_app.failures ApiFailure ValidationFailure]))

(defprotocol FailureToResponse
  (fail-to-response [this]))

(defn login-user [request]
  (let [body (get-in request [:parameters :body :user])
        api (get-in request [:apis :login-user])
        res (f/ok-> body
                     (v/validate-body v/login-user-schema)
                     api)]
    (if (f/ok? res)
      (ok {:user res})
      (fail-to-response res))))

(defn register-user [request]
  (let [body (get-in request [:parameters :body :user])
        api (get-in request [:apis :register-user])
        res (f/ok-> body
                    (v/validate-body v/register-user-schema)
                    api)]
    (if (f/ok? res)
      (ok {:user res})
      (fail-to-response res))))

(defn get-user [request]
  (let [user-id (:identity request)
        api (get-in request [:apis :get-user])
        res (api user-id)]
    (if (f/ok? res)
      (ok {:user res})
      (fail-to-response res))))

(defn update-user [request]
  (let [user-id (:identity request)
        body (get-in request [:parameters :body :user])
        api (get-in request [:apis :update-user])
        res (f/ok-> body
                    (v/validate-body v/update-user-schema)
                    (assoc :user-id user-id)
                    api)]
    (if (f/ok? res)
      (ok {:user res})
      (fail-to-response res))))

(extend-protocol FailureToResponse
  ValidationFailure
  (fail-to-response [this] (->> this
                                :errors
                                (map (fn [[field message]] [field [message]]))
                                (into {})
                                unprocessable-entity))

  ApiFailure
  (fail-to-response [this] (case (:error-type this)
                             :email-and-username-exist (unprocessable-entity {:errors {:email ["has already been taken"]
                                                                                       :username ["has already been taken"]}})
                             :email-exists (unprocessable-entity {:errors {:email ["has already been taken"]}})
                             :username-exists (unprocessable-entity {:errors {:username ["has already been taken"]}})
                             :email-or-password-invalid (unprocessable-entity {:errors {"email or password" ["is invalid"]}})
                             :user-not-found (not-found {:status 404 :error "Not Found"})
                             (internal-server-error {:error ["Oops, something went worng"]}))))

(comment

  (login-user {:parameters {:body {:email "123asd.com" :password "6900"}}})

  (require '[clojure.spec.alpha :as s]
           '[clojure.spec.gen.alpha :as gen]
           '[clojure-ring-reitit-datahike-realworld-example-app.failures :refer [api-fail]])

  (login-user {:parameters {:body {:email "123asd.com" :password ""}}
               :apis {:login-user (constantly (gen/generate (s/gen :clojure-ring-reitit-datahike-realworld-example-app.specs/user)))}})

  (login-user {:parameters {:body {:email "123@asd.com" :password "124556900"}}
               :apis {:login-user (constantly (gen/generate (s/gen :clojure-ring-reitit-datahike-realworld-example-app.specs/user)))}})

  (login-user {:parameters {:body {:email "123@asd.com" :password "124556900"}}
               :apis {:login-user (constantly (api-fail "oops" :oops-fail))}})

  (register-user {:parameters {:body {:username "hello" :email "hello" :password "34234"}}
                  :apis {:register-user (constantly (gen/generate (s/gen :clojure-ring-reitit-datahike-realworld-example-app.specs/user)))}})

  (register-user {:parameters {:body {:username "hello" :email "hello@hello.com" :password "1232334234"}}
                  :apis {:register-user (constantly (gen/generate (s/gen :clojure-ring-reitit-datahike-realworld-example-app.specs/user)))}})

  )
