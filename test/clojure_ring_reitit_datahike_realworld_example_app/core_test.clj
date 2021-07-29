(ns clojure-ring-reitit-datahike-realworld-example-app.core-test
  (:require [clojure-ring-reitit-datahike-realworld-example-app.core :as sut]
            [clojure-ring-reitit-datahike-realworld-example-app.token :refer [encode]]
            [clojure.test :as t]
            [datahike.api :as d]
            [integrant.core :as ig]
            [muuntaja.core :as m]
            [ring.mock.request :as mock]))

(def app)
(t/use-fixtures :each (fn [f] (let [system (ig/init (dissoc sut/config :adapter/jetty))
                                    db-conf (:db/conn sut/config)]
                                (with-redefs [app (:handler/app system)]
                                  (f))
                                (d/delete-database db-conf)
                                (ig/halt! system))))

(defn decode-json-body [rs]
  (update rs :body #(m/decode "application/json" %)))

(defn get-request
  ([uri] (get-request uri nil))
  ([uri token]
   (let [rs (app (cond-> (mock/request :get uri)
                   (some? token) (mock/header "Authorization" (str "Token " token))))]
     (decode-json-body rs))))

(defn post-request
  ([uri body]
   (post-request uri body nil))
  ([uri body token]
   (let [rs (app (cond-> (mock/request :post uri)
                                    (some? token) (mock/header "Authorization" (str "Token " token))
                                    true (mock/json-body body)))]
                      (decode-json-body rs))))

(defn put-request
  ([uri body]
   (put-request uri body nil))
  ([uri body token]
   (let [rs (app (cond-> (mock/request :put uri)
                   (some? token) (mock/header "Authorization" (str "Token " token))
                   true (mock/json-body body)))]
     (decode-json-body rs))))

(t/deftest test-register-user

  (t/testing "new user should be able to register"
    (let [user {:username "user1" :email "u1@u.com" :password "12345678"}
          rs (post-request "/api/users" {:user user})]
      (t/is (= 200 (:status rs)))
      (t/is (= (select-keys user [:username :email]) (select-keys (get-in rs [:body :user]) [:username :email])))
      (t/is (> (count (get-in rs [:body :user :token])) 0))))

  (t/testing "user with existing email cannot register"
    (let [user {:username "user11" :email "u1@u.com" :password "12345678"}
          rs (post-request "/api/users" {:user user})]
      (t/is (= 422 (:status rs)))
      (t/is (= ["has already been taken"] (get-in rs [:body :errors :email])))))

  (t/testing "user with existing username cannot register"
    (let [user {:username "user1" :email "u11@u.com" :password "12345678"}
          rs (post-request "/api/users" {:user user})]
      (t/is (= 422 (:status rs)))
      (t/is (= ["has already been taken"] (get-in rs [:body :errors :username])))))

  (t/testing "user with invalid input cannot register"
    (let [user {:username "" :email "u11u.com" :password "1238"}
          rs (post-request "/api/users" {:user user})]
      (t/is (= 422 (:status rs)))
      (t/is (= ["can't be blank"] (get-in rs [:body :errors :username])))
      (t/is (= ["is invalid"] (get-in rs [:body :errors :email])))
      (t/is (= ["is too short (minimum is 8 characters)"] (get-in rs [:body :errors :password]))))))

(t/deftest test-login-user
  (let [user {:username "user1" :email "u1@u.com" :password "12345678"}
        _ (post-request "/api/users" {:user user})]

    (t/testing "exising user should be able to login"
      (let [rs (post-request "/api/users/login" {:user (dissoc user :username)})]
        (t/is (= 200 (:status rs)))
        (t/is (= (select-keys user [:username :email]) (select-keys (get-in rs [:body :user]) [:username :email])))
        (t/is (> (count (get-in rs [:body :user :token])) 0))))

    (t/testing "exising user with wrong password cannot loggin"
      (let [rs (post-request "/api/users/login" {:user (-> user
                                                           (dissoc :username)
                                                           (assoc :password "abcdefghig"))})]
        (t/is (= 422 (:status rs)))
        (t/is (= ["is invalid"] (get-in rs [:body :errors (keyword "email or password")])))))

    (t/testing "not exising user cannot login"
      (let [rs (post-request "/api/users/login" {:user {:email "u2@u.com" :password "12345678"}})]
        (t/is (= 422 (:status rs)))
        (t/is (= ["is invalid"] (get-in rs [:body :errors (keyword "email or password")])))))))

(t/deftest test-get-user
  (let [user {:username "user1" :email "u1@u.com" :password "12345678"}
        rs (post-request "/api/users" {:user user})
        token (get-in rs [:body :user :token])]

    (t/testing "authenticated user should get self back"
      (let [rs (get-request "/api/user" token)]
        (t/is (= 200 (:status rs)))
        (t/is (= (select-keys user [:username :email]) (select-keys (get-in rs [:body :user]) [:username :email])))
        (t/is (> (count (get-in rs [:body :user :token])) 0))))

    (t/testing "authenticated user should get not found when user not exists"
      (let [rs (get-request "/api/user" (encode (get-in sut/config [:services/token :jwt-key]) (get-in sut/config [:services/token :jwt-expiration-minutes]) "user-id-123"))]
        (t/is (= 404 (:status rs)))
        (t/is (= "Not Found" (get-in rs [:body :error])))))

    (t/testing "non authenticated user should get unauthorized"
      (let [rs (get-request "/api/user")]
        (t/is (= 401 (:status rs)))))

    (t/testing "user with invalid token should get unauthorized"
      (let [rs (get-request "/api/user" (encode "foo-bar" (get-in sut/config [:services/token :jwt-expiration-minutes]) "user-id-123"))]
        (t/is (= 401 (:status rs)))))

    (t/testing "user with expired token should get unauthorized"
      (let [rs (get-request "/api/user" (encode (get-in sut/config [:services/token :jwt-key]) -1 "user-id-123"))]
        (t/is (= 401 (:status rs)))))))

(t/deftest test-update-user
  (let [user {:username "user1" :email "u1@u.com" :password "12345678"}
        rs (post-request "/api/users" {:user user})
        token (get-in rs [:body :user :token])]

    (t/testing "user should update self"
      (let [update-user {:username "user2" :email "u1@u.com" :bio "bio-bio" :image "image1.jpg"}
            rs (put-request "/api/user" {:user update-user} token)]
        (t/is (= 200 (:status rs)))
        (t/is (= (:username update-user) (get-in rs [:body :user :username])))
        (t/is (= (:email user) (get-in rs [:body :user :email])))
        (t/is (= (select-keys update-user [:bio :image]) (select-keys (get-in rs [:body :user]) [:bio :image])))
        (t/is (> (count (get-in rs [:body :user :token])) 0))))

    (t/testing "user cannot update email to already taken"
      (let [_ (post-request "/api/users" {:user {:username "user3" :email "u3@u.com" :password "12345678"}})
            update-user {:username "user1" :email "u3@u.com"}
            rs (put-request "/api/user" {:user update-user} token)]
        (t/is (= 422 (:status rs)))
        (t/is (= ["has already been taken"] (get-in rs [:body :errors :email])))))

    (t/testing "user cannot update username to already taken"
      (let [_ (post-request "/api/users" {:user {:username "user4" :email "u4@u.com" :password "12345678"}})
            update-user {:username "user4" :email "u1@u.com"}
            rs (put-request "/api/user" {:user update-user} token)]
        (t/is (= 422 (:status rs)))
        (t/is (= ["has already been taken"] (get-in rs [:body :errors :username])))))

    (t/testing "non authenticated user should get unauthorized"
      (let [rs (put-request "/api/user" {:user {}})]
        (t/is (= 401 (:status rs)))))))
