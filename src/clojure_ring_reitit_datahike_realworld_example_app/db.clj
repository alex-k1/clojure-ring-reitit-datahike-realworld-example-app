(ns clojure-ring-reitit-datahike-realworld-example-app.db
  (:require [clojure.walk :refer [postwalk]]
            [datahike.api :as d]
            [datahike.core :refer [resolve-tempid squuid]]))

(def schema [;; user
             {:db/ident :user/user-id
              :db/valueType :db.type/uuid
              :db/cardinality :db.cardinality/one
              :db/unique :db.unique/identity}
             {:db/ident :user/username
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/unique :db.unique/identity}
             {:db/ident :user/email
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one
              :db/unique :db.unique/identity}
             {:db/ident :user/password
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident :user/bio
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one}
             {:db/ident :user/image
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one}
             ])

(def user-pattern '[(:user/user-id :as user-id)
                    (:user/username :as :username)
                    (:user/email :as :email)
                    (:user/password :as :password)
                    (:user/bio :as :bio :default nil)
                    (:user/image :as :image :default nil)])

(defn- add-ns [m ns]
  (postwalk #(if (keyword? %) (keyword ns (name %)) %) m))

(defn- generate-id []
  (squuid))

(defn find-user-by-user-id [conn user-id]
  (d/q '[:find (pull ?e pattetn) .
         :in $ pattetn ?user-id
         :where
         [?e :user/user-id ?user-id]]
       @conn user-pattern user-id))

(defn find-user-by-email [conn email]
  (d/q '[:find (pull ?e pattern) .
         :in $ pattern ?email
         :where
         [?e :user/email ?email]]
       @conn user-pattern email))

(defn find-user-by-username [conn username]
  (d/q '[:find (pull ?e pattern) .
         :in $ pattern ?username
         :where
         [?e :user/username ?username]]
       @conn user-pattern username))

(defn add-user [conn user]
  (let [id (generate-id)
        tx-data (-> user
                    (assoc :user-id id)
                    (add-ns "user"))]
    (d/transact conn [tx-data])
    (find-user-by-user-id conn id)))

(defn update-user [conn user]
  (d/transact conn [(add-ns user "user")])
  (find-user-by-user-id conn (:user-id user)))

(comment

  (def conf {:store {:backend :mem :id "mem-db-repl"}
             :initial-tx schema})

  (def conn
    (do
      (when-not (d/database-exists? conf)
        (d/create-database conf))
      (d/connect conf)))

  (d/q '[:find [(pull ?e [:user/username :user/email]) ...]
         :where
         [?e :user/username ?n]]
       @conn)
;; => [#:user{:username "Mary Smith", :email "mary@smith.com"} #:user{:username "John Smith", :email "john@smith.com"}]

  (def p '[(:user/user-id :as user-id) (:user/username :as :username) (:user/email :as :email) (:user/password :as :password) (:user/bio :as :bio) (:user/image :as :image)])

  (d/q '[:find (pull ?e pattern) .
         :in $ ?username pattern
         :where
         [?e :user/username ?username]]
       @conn "John Smith" p)

  (def user-id-1 (squuid))
  (def user-id-2 (squuid))
  (d/transact conn [{:user/user-id user-id-1 :user/username "John Smith" :user/email "john@smith.com" :user/password "123" :user/bio "la-la-la1"}])
  (d/transact conn [{:user/user-id user-id-2 :user/username "Mary Smith" :user/email "mary@smith.com" :user/password "346" :user/bio "bla-bla-bla"}])

  (d/transact conn [#:user{:username "David N", :email "d@n.com", :password "12345", :user-id #uuid "60e49578-41de-42c6-8e04-20817460844a"}])

  (find-user-by-user-id conn user-id-1)
  (find-user-by-user-id conn user-id-2)
  (find-user-by-user-id conn (squuid))
  (find-user-by-user-id conn "ddfdf")

  (find-user-by-email conn "john@smith.com")
  (find-user-by-username conn "John Smith")

  (add-user conn {:username "James C" :email "j@c.com" :password "12345"})
;; => {user-id #uuid "60e49e8a-a41e-4019-b46a-c6420cbd9c0b", :username "James C", :email "j@c.com", :password "12345"}
  (add-user conn {:username "Clive B" :email "c@b.com" :password "12345"})
;; => {user-id #uuid "60e49e8d-0a2b-441c-ae4c-be8d6c795910", :username "Clive B", :email "c@b.com", :password "12345"}

  (update-user conn {:user-id #uuid "60e49e8d-0a2b-441c-ae4c-be8d6c795910", :username "Clive C" :bio "yup"})
;; => {user-id #uuid "60e49e8d-0a2b-441c-ae4c-be8d6c795910", :username "Clive C", :email "c@b.com", :password "12345", :bio "yup"}

  (find-user-by-username conn "David N")
  (find-user-by-username conn "James C")
  (find-user-by-username conn "Clive B")
  (find-user-by-username conn "Clive C")

  (d/q '[:find (pull ?e [:db/id :user/username :user/email])
         :in $ pattern
         :where
         [?e :user/username _]]
       @conn p)

  (d/transact conn [{:user/user-id user-id-1 :user/username "Mary Smith2" :user/email "mary@smith2.com" :user/password "346" :user/bio "bla-bla-bla"}])
;; => {:db-before #datahike/DB {:max-tx 536870919 :max-eid 10},
;;     :db-after #datahike/DB {:max-tx 536870920 :max-eid 10},
;;     :tx-data
;;     [#datahike/Datom [536870920
;;                       :db/txInstant
;;                       #inst "2021-07-06T17:47:46.558-00:00"
;;                       536870920]
;;      #datahike/Datom [7
;;                       :user/user-id
;;                       #uuid "60e48dc5-71be-4633-a016-2eca386d063b"
;;                       536870920]
;;      #datahike/Datom [7 :user/username "Mary Smith2" 536870920]
;;      #datahike/Datom [7 :user/email "mary@smith2.com" 536870920]
;;      #datahike/Datom [7 :user/password "346" 536870920]
;;      #datahike/Datom [7 :user/bio "bla-bla-bla" 536870920]],
;;     :tempids #:db{:current-tx 536870920},
;;     :tx-meta nil}
  (d/transact conn [{:key1 "value1" :key2 "value2"}])

  (d/q '[:find ?e ?n ?m ?b
         :where
         [?e :user/username ?n]
         [?e :user/email ?m]
         [?e :user/bio ?b]
         [?_ :db/id 71]]
       @conn)

  ;; find by entity id
  (d/q '[:find (pull ?e p) .
         :in $ p ?e
         :where [?e]] @conn p 7)

  (squuid)

  (def t (d/tempid @conn))
  (d/transact conn [{:db/id t :user/username "Mary Smith2" :user/email "mary@smith2.com" :user/password "346" :user/bio "bla-bla-bla"}])
;; => {:db-before #datahike/DB {:max-tx 536870932 :max-eid 8},
;;     :db-after #datahike/DB {:max-tx 536870933 :max-eid 8},
;;     :tx-data
;;     [#datahike/Datom [536870933
;;                       :db/txInstant
;;                       #inst "2021-07-05T03:18:45.190-00:00"
;;                       536870933]
;;      #datahike/Datom [8 :user/username "Mary Smith2" 536870933]
;;      #datahike/Datom [8 :user/email "mary@smith2.com" 536870933]
;;      #datahike/Datom [8 :user/password "346" 536870933]
;;      #datahike/Datom [8 :user/bio "bla-bla-bla" 536870933]],
;;     :tempids {-1000012 8, :db/current-tx 536870933},
;;     :tx-meta nil}
  (resolve-tempid @conn {-1000012 8, :db/current-tx 536870933} t)

  conn

  (d/release conn)

  (d/delete-database conf)

  (keyword "foo")

  (postwalk (fn [x] (if (keyword? x) (keyword "foo" (name x)) x)) {:a 1 :b 2 :c 3})
  (postwalk (fn [x] (if (keyword? x) (keyword "foo" (name x)) x)) {:a 1 :b 2 :c {:d 3 :e 4}})
)
