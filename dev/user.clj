(ns user
  (:require [integrant.repl :refer [go halt set-prep!]]
            [clojure-ring-reitit-datahike-realworld-example-app.specs]
            [clojure-ring-reitit-datahike-realworld-example-app.core :refer [config]]))

(set-prep! (constantly config))

(defn start []
  (go))

(defn stop []
  (halt))

(defn reset []
  (stop)
  (start))
