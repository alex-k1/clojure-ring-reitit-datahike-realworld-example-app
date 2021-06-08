(ns clojure-ring-reitit-datahike-realworld-example-app.core-test
  (:require [clojure.test :refer [deftest is]]
            [clojure-ring-reitit-datahike-realworld-example-app.core :refer [health-check-handler]]
            [ring.mock.request :as mock]))

(deftest health-check-handler-test
  (let [response (health-check-handler (mock/request :get "api/health-check"))]
    (is (= (:status response) 200))
    (is (= (:body response) "I am alive"))))
