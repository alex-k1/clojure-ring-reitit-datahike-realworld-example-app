(ns clojure-ring-reitit-datahike-realworld-example-app.apis
  (:require [clojure-ring-reitit-datahike-realworld-example-app.failures :as fail]
            [crypto.password.scrypt :as password]))

(defn- safe-check [raw encrypted]
  (try
    (password/check raw encrypted)
    (catch Exception _ false)))

(defn- email-username-exist-failure [user-by-email user-by-username]
  (cond
    (and user-by-email user-by-username) fail/email-and-username-exist-fail
    user-by-email fail/email-exists-fail
    user-by-username fail/username-exists-fail
    :else nil))

(defn login-user [{:keys [find-user-by-email generate-token]} {:keys [email password]}]
  (if-let [user (find-user-by-email email)]
    (if (safe-check password (:password user))
      (-> user
          (dissoc :password)
          (assoc :token (generate-token (:user-id user))))
      fail/password-invalid-fail)
    fail/email-invalid-fail))

(defn register-user [{:keys [find-user-by-email find-user-by-username add-user generate-token]} {:keys [username email] :as user-data}]
  (if-let [fail (email-username-exist-failure (find-user-by-email email) (find-user-by-username username))]
    fail
    (let [user (add-user (update user-data :password password/encrypt))]
      (-> user
          (dissoc :password)
          (assoc :token (generate-token (:user-id user)))))))

(defn get-user [{:keys [find-user-by-id generate-token]} {:keys [user-id]}]
  (if-let [user (find-user-by-id user-id)]
    (-> user
        (dissoc :password)
        (assoc :token (generate-token (:user-id user))))
    fail/user-not-found-fail))

(defn update-user [{:keys [find-user-by-email find-user-by-username update-user generate-token]} {:keys [username email] :as user-data}]
  (if-let [fail (email-username-exist-failure (find-user-by-email email) (find-user-by-username username))]
    fail
    (if-let [user (update-user user-data)]
      (-> user
          (dissoc :password)
          (assoc :token (generate-token (:user-id user))))
      fail/user-not-found-fail)))

(comment

  (password/encrypt "hello1")

  (password/check "hello" "$s0$f0801$7pXCIXbRUmO0ZkQ7bxNw1Q==$Xlpt4pMk809hvNH+am/pp4oLuRPu6X9po+hhT2DNw08=")

  (password/check "hello" "$s0$f0801$+Ed448U6priCawgo165cTg==$H+5ZGuZI//EwetwUdO4uh2SxAO0/qrvwuN6j3rKVYbU=")

  (require '[clojure.spec.alpha :as s]
            '[clojure.spec.gen.alpha :as gen]
            '[clojure-ring-reitit-datahike-realworld-example-app.token :as token])

  (def generate-token (partial token/jwt-token "key" 10))

  (def login-user-body (gen/generate (s/gen :login/user)))
  (def user1 (update login-user-body :password password/encrypt))
  (def user2 (assoc login-user-body :password (password/encrypt "wrong-password")))

  (login-user {:find-user-by-email (constantly user1) :generate-token generate-token} login-user-body)

  (login-user {:find-user-by-email (constantly user2) :generate-token generate-token} login-user-body)

  (login-user {:find-user-by-email (constantly nil) :generate-token generate-token} login-user-body)

  (def register-user-body (gen/generate (s/gen :register/user)))
  (def user1 (update register-user-body :password password/encrypt))

  (register-user {:find-user-by-email (constantly nil) :find-user-by-username (constantly nil) :add-user (constantly user1) :generate-token generate-token} register-user-body)
  (register-user {:find-user-by-email (constantly user1) :find-user-by-username (constantly nil) :add-user (constantly user1) :generate-token generate-token} register-user-body)
  (register-user {:find-user-by-email (constantly nil) :find-user-by-username (constantly user1) :add-user (constantly user1) :generate-token generate-token} register-user-body)
  (register-user {:find-user-by-email (constantly user1) :find-user-by-username (constantly user1) :add-user (constantly user1) :generate-token generate-token} register-user-body)

  (get-user {:find-user-by-id (constantly nil) :generate-token generate-token} {:user-id 1})
  (get-user {:find-user-by-id (constantly user1) :generate-token generate-token} {:user-id 1})

  (update-user {:find-user-by-email (constantly nil) :find-user-by-username (constantly nil) :update-user (constantly user1) :generate-token generate-token} register-user-body)
  (update-user {:find-user-by-email (constantly nil) :find-user-by-username (constantly nil) :update-user (constantly nil) :generate-token generate-token} register-user-body)
  (update-user {:find-user-by-email (constantly user1) :find-user-by-username (constantly nil) :update-user (constantly user1) :generate-token generate-token} register-user-body)
  (update-user {:find-user-by-email (constantly nil) :find-user-by-username (constantly user1) :update-user (constantly user1) :generate-token generate-token} register-user-body)
  (update-user {:find-user-by-email (constantly user1) :find-user-by-username (constantly user1) :update-user (constantly user1) :generate-token generate-token} register-user-body)

  )
