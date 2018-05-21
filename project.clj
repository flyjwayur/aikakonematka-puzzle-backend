(defproject aikakonematka-puzzle-backend "0.1.0-SNAPSHOT"
  :description "Backend for aikakonematka-puzzle"
  :source-paths ["src"]
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [cheshire "5.8.0"]
                 [clojure.java-time "0.3.2"]
                 [compojure "1.6.0"]
                 [com.taoensso/sente "1.12.0"]
                 [environ "1.0.1"]
                 [http-kit "2.2.0"]
                 [ring/ring-defaults "0.2.0"]
                 [ring-cors "0.1.7"]]
  :main ^:skip-aot aikakonematka-puzzle-backend.core
  :uberjar-name "aikakone-backend-standalone.jar"
  :min-lein-version "2.0.0"
  :hooks [environ.leiningen.hooks]
  :profiles {:production {:env {:production true}}})
