(ns clojure-ring-reitit-datahike-realworld-example-app.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::email string?)
(s/def ::password string?)
(s/def ::token string?)
(s/def ::username string?)
(s/def ::bio (s/nilable string?))
(s/def ::image (s/nilable string?))

(s/def :login/user (s/keys :req-un [::email ::password]))
(s/def :register/user (s/keys :req-un [::username ::email ::pasword]))
(s/def :update/user (s/keys :opt-un [::username ::email ::password ::bio ::image]))
(s/def ::user (s/keys :req-un [::email ::token ::username ::bio ::image]))

(s/def ::login-user-request (s/keys :req-un [:login/user]))
(s/def ::login-user-response (s/keys :req-un [::user]))

(s/def ::register-user-request (s/keys :req-un [:register/user]))
(s/def ::register-user-response (s/keys :req-un [::user]))

(s/def ::get-user-response (s/keys :req-un [::user]))

(s/def ::update-user-request (s/keys :req-un [:update/user]))
(s/def ::update-user-response (s/keys :req-un [::user]))

(comment

  (s/valid? :login/user {:email "hello@email.com" :password "12345"})

  (s/valid? ::login-user-request {:user {:email "hello@email.com" :password "12345"}})

  (s/valid? :update/user {:username "hello3"})
  (s/valid? :update/user {:username "hello3" :email "new-email@email.com"})
  (s/valid? :update/user {:bio nil :image "new-image"})
  (s/valid? :update/user {})

  (s/valid? ::user {:email "hello@world.com" :token "token123" :username "user1" :bio "bio" :image nil})

  (s/valid? ::get-user-response {:user {:email "hello@world.com" :token "token123" :username "user1" :bio "bio" :image nil}})

  )
