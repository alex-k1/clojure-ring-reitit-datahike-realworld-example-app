(ns clojure-ring-reitit-datahike-realworld-example-app.token
  (:require [buddy.sign.jwt :as jwt]
            [java-time :as t]))

(defn encode [key expire-in-minutes user-id]
  (let [exp (-> (t/instant)
                (t/plus (t/minutes expire-in-minutes))
                t/to-millis-from-epoch
                (/ 1000))
        claims {:user-id user-id :exp exp}]
    (jwt/sign claims key)))

(defn decode [key token]
  (try
    (-> token
        (jwt/unsign key)
        :user-id)
    (catch Exception _ nil)))

(comment
  (jwt/sign {:user-id 22} "key")

  (jwt/unsign "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyLWlkIjoyMn0.ZkMGuTjjbC9stKCLC6-IGH9Tkv0dud2T-28dd3ywSD8" "key")

  (def exp (-> (t/instant)
               (t/plus (t/seconds 10))
               t/to-millis-from-epoch))

  (def t (jwt/sign {:user-id 22 :exp exp} "key"))

  (def now (-> (t/instant)
               t/to-millis-from-epoch))

  (jwt/unsign t "key" {:now now})

  (encode "key" 1 "user-id")

  (decode "key" "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyLWlkIjoidXNlci1pZCIsImV4cCI6MS42MjU5NTI5NjYwMzZFOX0.7j7atSqcz-Ktlb0BKw9vUWa01_Rij7NA-dwaiWFq8Ko")

)
