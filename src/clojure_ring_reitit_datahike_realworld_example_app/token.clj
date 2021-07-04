(ns clojure-ring-reitit-datahike-realworld-example-app.token
  (:require [buddy.sign.jwt :as jwt]
            [java-time :as t]))

(defn jwt-token [key expire-in-minutes user-id]
  (let [exp (-> (t/instant)
                (t/plus (t/minutes expire-in-minutes))
                t/to-millis-from-epoch)
        claims {:user-id user-id :exp exp}]
    (jwt/sign claims key)))

(comment
  (jwt/sign {:user-id 22} "key")

  (jwt/unsign "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyLWlkIjoyMn0.ZkMGuTjjbC9stKCLC6-IGH9Tkv0dud2T-28dd3ywSD8" "key")

  (def exp (-> (t/instant)
               (t/plus (t/seconds 10))
               t/to-millis-from-epoch))

  (def jwt (jwt/sign {:user-id 22 :exp exp} "key"))

  (def now (-> (t/instant)
               t/to-millis-from-epoch))

  (jwt/unsign jwt "key" {:now now})

  (jwt-token "key" 10 "user-id")

)
