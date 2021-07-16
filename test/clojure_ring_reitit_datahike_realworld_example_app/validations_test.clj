(ns clojure-ring-reitit-datahike-realworld-example-app.validations-test
  (:require [clojure-ring-reitit-datahike-realworld-example-app.validations :as sut]
            [clojure.test :as t]
            [struct.core :as st]))

(t/deftest test-trim-coerse-required
  (let [schema [[:field1 sut/trim-coerce st/required]
                [:field2 st/required]]]
    (t/is (= "trimmed" (-> {:field1 "   trimmed  " :field2 "f2"}
                           (sut/validate-body schema)
                           :field1)))))

(t/deftest test-trim-coerse-optional
  (let [schema [[:field1 sut/trim-coerce]
                [:field2 st/required]]]
    (t/is (nil? (-> {:field2 "f2"}
                    (sut/validate-body schema)
                    :field1)))))

(t/deftest test-lower-coerse
  (let [schema [[:field1 st/string sut/lower-coerce]
                [:field2 st/required]]]
    (t/is (= "lower" (-> {:field1 "LOweR" :field2 "f2"}
                         (sut/validate-body schema)
                         :field1)))))

(t/deftest test-multiple-custom-coerces
  (let [schema [[:field1 sut/trim-coerce st/string sut/lower-coerce]
                [:field2 st/required]]]
    (t/is (= "lower trimmed" (-> {:field1 "  LOweR TriMMed     " :field2 "f2"}
                                 (sut/validate-body schema)
                                 :field1)))))

(t/deftest test-validate-body-remove-unknown-keys
  (let [schema [[:field1 st/string]
                [:field2 st/required]]]
    (t/is (nil? (-> {:field1 "f1" :field2 "f2" :field3 "f3"}
                    (sut/validate-body schema)
                    :field3)))))

;; login user schema

(t/deftest test-valid-login-user-schema
  (let [login-body {:email "  Email@email.coM " :password "12345678  "}
        validated-body (sut/validate-body login-body sut/login-user-schema)]
    (t/is (nil? (:errors validated-body)))
    (t/is (= "email@email.com" (:email validated-body)))
    (t/is (= (:password login-body) (:password validated-body)))))

(t/deftest test-empty-password-login-user-schema
  (let [login-body {:email "  Email@email.coM " :password ""}
        validated-body (sut/validate-body login-body sut/login-user-schema)]
    (t/is (= "can't be blank" (get-in validated-body [:errors :password])))
    (t/is (nil? (get-in validated-body [:errors :email])))))

(t/deftest test-empty-email-password-login-user-schema
  (let [login-body {:email "   " :password ""}
        validated-body (sut/validate-body login-body sut/login-user-schema)]
    (t/is (= "can't be blank" (get-in validated-body [:errors :email])))
    (t/is (= "can't be blank" (get-in validated-body [:errors :password])))))

(t/deftest test-invalid-email-login-user-schema
  (let [login-body {:email "email@com" :password "12345678"}
        validated-body (sut/validate-body login-body sut/login-user-schema)]
    (t/is (= "is invalid" (get-in validated-body [:errors :email])))
    (t/is (nil? (get-in validated-body [:errors :password])))))

;; register user schema

(t/deftest test-valid-register-user-schema
  (let [register-body {:username "user1 " :email "  Email@email.coM " :password "12345678  "}
        validated-body (sut/validate-body register-body sut/register-user-schema)]
    (t/is (nil? (:errors validated-body)))
    (t/is (= "user1" (:username validated-body)))
    (t/is (= "email@email.com" (:email validated-body)))
    (t/is (= (:password register-body) (:password validated-body)))))

(t/deftest test-empty-username-register-user-schema
  (let [register-body {:username "" :email "  Email@email.coM " :password "12345678"}
        validated-body (sut/validate-body register-body sut/register-user-schema)]
    (t/is (= "can't be blank" (get-in validated-body [:errors :username])))
    (t/is (nil? (get-in validated-body [:errors :email])))
    (t/is (nil? (get-in validated-body [:errors :password])))))

(t/deftest test-empty-username-email-password-register-user-schema
  (let [register-body {:email "   " :password ""}
        validated-body (sut/validate-body register-body sut/register-user-schema)]
    (t/is (= "can't be blank" (get-in validated-body [:errors :username])))
    (t/is (= "can't be blank" (get-in validated-body [:errors :email])))
    (t/is (= "can't be blank" (get-in validated-body [:errors :password])))))

(t/deftest test-too-long-fields-register-user-schema
  (let [register-body {:username (apply str (repeat 51 "A")) :email (str (apply str (repeat 251 "A")) "email@sad.com") :password (apply str (repeat 101 "A"))}
        validated-body (sut/validate-body register-body sut/register-user-schema)]
    (t/is (= "is too long (maximum is 50 character)" (get-in validated-body [:errors :username])))
    (t/is (= "is too long (maximum is 250 character)" (get-in validated-body [:errors :email])))
    (t/is (= "is too long (maximum is 100 character)" (get-in validated-body [:errors :password])))))

(t/deftest test-too-short-password-register-user-schema
  (let [register-body {:username "user" :email "email@email.com" :password "123"}
        validated-body (sut/validate-body register-body sut/register-user-schema)]
    (t/is (= "is too short (minimum is 8 character)" (get-in validated-body [:errors :password])))
    (t/is (nil? (get-in validated-body [:errors :username])))
    (t/is (nil? (get-in validated-body [:errors :email])))))

(t/deftest test-invalid-email-register-user-schema
  (let [register-body {:username "user1" :email "email@com" :password "12345678"}
        validated-body (sut/validate-body register-body sut/register-user-schema)]
    (t/is (= "is invalid" (get-in validated-body [:errors :email])))
    (t/is (nil? (get-in validated-body [:errors :username])))
    (t/is (nil? (get-in validated-body [:errors :password])))))

;; update user schema

(t/deftest test-valid-without-optional-fields-upate-user-schema
  (let [update-body {:username "user1  " :email "  Email@email.coM "}
        validated-body (sut/validate-body update-body sut/update-user-schema)]
    (t/is (nil? (:errors validated-body)))
    (t/is (= "user1" (:username validated-body)))
    (t/is (= "email@email.com" (:email validated-body)))))

(t/deftest test-valid-with-optional-fields-upate-user-schema
  (let [update-body {:username "  user1" :email "  Email@email.coM " :password "12345678" :image "  image  " :bio "bio  "}
        validated-body (sut/validate-body update-body sut/update-user-schema)]
    (t/is (nil? (:errors validated-body)))
    (t/is (= "user1" (:username validated-body)))
    (t/is (= "email@email.com" (:email validated-body)))
    (t/is (= (:password update-body) (:password validated-body)))
    (t/is (= "image" (:image validated-body)))
    (t/is (= "bio" (:bio validated-body)))))

(t/deftest test-empty-username-update-user-schema
  (let [update-body {:username "" :email "  Email@email.coM " :bio "bio"}
        validated-body (sut/validate-body update-body sut/update-user-schema)]
    (t/is (= "can't be blank" (get-in validated-body [:errors :username])))
    (t/is (nil? (get-in validated-body [:errors :email])))
    (t/is (nil? (get-in validated-body [:errors :password])))
    (t/is (nil? (get-in validated-body [:errors :bio])))
    (t/is (nil? (get-in validated-body [:errors :image])))))

(t/deftest test-empty-username-email-update-user-schema
  (let [update-body {:email "   " :image "image"}
        validated-body (sut/validate-body update-body sut/update-user-schema)]
    (t/is (= "can't be blank" (get-in validated-body [:errors :username])))
    (t/is (= "can't be blank" (get-in validated-body [:errors :email])))
    (t/is (nil? (get-in validated-body [:errors :password])))
    (t/is (nil? (get-in validated-body [:errors :bio])))
    (t/is (nil? (get-in validated-body [:errors :image])))))

(t/deftest test-too-long-fields-update-user-schema
  (let [update-body {:username (apply str (repeat 51 "A")) :email (str (apply str (repeat 251 "A")) "email@sad.com") :password (apply str (repeat 101 "A")) :image (apply str (repeat 501 "A")) :bio (apply str (repeat 1001 "A"))}
        validated-body (sut/validate-body update-body sut/update-user-schema)]
    (t/is (= "is too long (maximum is 50 character)" (get-in validated-body [:errors :username])))
    (t/is (= "is too long (maximum is 250 character)" (get-in validated-body [:errors :email])))
    (t/is (= "is too long (maximum is 100 character)" (get-in validated-body [:errors :password])))
    (t/is (= "is too long (maximum is 1000 character)" (get-in validated-body [:errors :bio])))
    (t/is (= "is too long (maximum is 500 character)" (get-in validated-body [:errors :image])))))

(t/deftest test-too-short-password-update-user-schema
  (let [update-body {:username "user" :email "email@email.com" :password "123"}
        validated-body (sut/validate-body update-body sut/update-user-schema)]
    (t/is (= "is too short (minimum is 8 character)" (get-in validated-body [:errors :password])))
    (t/is (nil? (get-in validated-body [:errors :username])))
    (t/is (nil? (get-in validated-body [:errors :email])))))

(t/deftest test-invalid-email-update-user-schema
  (let [update-body {:username "user1" :email "email@com" :password "12345678"}
        validated-body (sut/validate-body update-body sut/update-user-schema)]
    (t/is (= "is invalid" (get-in validated-body [:errors :email])))
    (t/is (nil? (get-in validated-body [:errors :username])))
    (t/is (nil? (get-in validated-body [:errors :password])))))
