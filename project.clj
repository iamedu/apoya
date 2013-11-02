(defproject apoya "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1978"]
                 [org.clojure/tools.cli "0.2.4"]
                 [org.clojure/tools.logging "0.2.6"]
                 [fortress-ring-adapter "0.1.0-SNAPSHOT"]
                 [jarohen/nomad "0.5.1"]
                 [ch.qos.logback/logback-core "1.0.13"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.jclouds/jclouds-allblobstore "1.6.0"]
                 [org.postgresql/postgresql "9.2-1003-jdbc4"]
                 [ragtime "0.3.4"]]
  :source-paths  ["src/clj"]
  :plugins [[lein-cljsbuild "0.3.4"]
            [ragtime/ragtime.lein "0.3.4"]]
  :cljsbuild {;;:crossovers [uxtweet.front]
              :crossover-path "src/crossover-cljs"
              :builds {:main {:source-paths ["src/cljs"]
                              :compiler {:output-to "resources/public/js/main.js"
                                         :externs  ["externs/angular.js"]
                                         :optimizations :whitespace
                                         :pretty-print true}}}}
  :ragtime  {:migrations ragtime.sql.files/migrations
             :database "jdbc:postgresql:apoya"}
  :main ^:skip-aot apoya.core
  :target-path "target/%s"
  :jvm-opts ["-Xbootclasspath/p:lib/npn-boot-1.1.6.v20130911.jar"]
  :profiles {:uberjar {:aot :all}})
