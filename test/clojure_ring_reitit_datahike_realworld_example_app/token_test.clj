(ns clojure-ring-reitit-datahike-realworld-example-app.token-test
  (:require [clojure-ring-reitit-datahike-realworld-example-app.token :as sut]
            [clojure.test :as t]))

(t/deftest test-valid-token
  (let [user-id "user-1"
        key "key"
        token (sut/encode key 10 user-id)]
    (t/is (= user-id (sut/decode key token)))))

(t/deftest test-invalid-token
  (let [user-id "user-1"
        key "key"
        token (sut/encode key 10 user-id)]
    (t/is (= nil (sut/decode key (str "x" token "x"))))))

(t/deftest test-expired-token
  (let [user-id "user-1"
        key "key"
        token (sut/encode key -10 user-id)]
    (t/is (= nil (sut/decode key token)))))
