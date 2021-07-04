(ns clojure-ring-reitit-datahike-realworld-example-app.handlers
  (:require [clojure-ring-reitit-datahike-realworld-example-app.failures :refer [format-failure]]
            [clojure-ring-reitit-datahike-realworld-example-app.validations :as v]
            [failjure.core :as f]
            [ring.util.http-response :refer [not-found ok unprocessable-entity]]))

(defn login-user [request]
  (let [body (get-in request [:parameters :body])
        api (get-in request [:apis :login-user])
        res (f/ok-> body
                     (v/validate-body v/login-user-schema)
                     api)]
    (if (f/ok? res)
      (ok {:user res})
      (unprocessable-entity {:errors (format-failure res)}))))

(defn register-user [request]
  (let [body (get-in request [:parameters :body])
        api (get-in request [:apis :register-user])
        res (f/ok-> body
                    (v/validate-body v/register-user-schema)
                    api)]
    (if (f/ok? res)
      (ok {:user res})
      (unprocessable-entity {:errors (format-failure res)}))))

(defn get-user [request]
  (let [user-id (:user-id request) ;; TODO check where it is injected to
        api (get-in request [:apis :get-user])
        res (api user-id)]
    (if (f/ok? res)
      (ok {:user res})
      (not-found {:status "404" :error "Not Found"}))))

(defn update-user [request])

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
