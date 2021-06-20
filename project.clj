(defproject clojure-ring-reitit-datahike-realworld-example-app "0.1.0-SNAPSHOT"
  :description "RealWorld Example App"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.3"]

                 [ring/ring-core "1.9.3"]
                 [ring/ring-jetty-adapter "1.9.3"]

                 [metosin/reitit-core "0.5.13"]
                 [metosin/reitit-middleware "0.5.13"]
                 [metosin/reitit-spec "0.5.13"]
                 [metosin/ring-http-response "0.9.2"]
                 [metosin/muuntaja "0.6.8"]

                 [funcool/struct "1.4.0"]

                 [failjure "2.2.0"]

                 [integrant "0.8.0"]

                 [io.replikativ/datahike "0.3.6"]]

  :profiles {:dev {:dependencies [[ring/ring-mock "0.4.0"]
                                  [integrant/repl "0.3.2"]
                                  [org.clojure/test.check "1.1.0"]]
                   :source-paths ["dev"]
                   :repl-options {:init-ns user}}}

  :repl-options {:init-ns clojure-ring-reitit-datahike-realworld-example-app.core})
