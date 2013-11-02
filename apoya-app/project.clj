(defproject apoya "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1978"]
                 [org.clojure/tools.cli "0.2.4"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [ring/ring-core "1.2.0"]
                 [ring/ring-devel "1.2.0"]
                 [fortress-ring-adapter "0.1.0-SNAPSHOT"]
                 [compojure "1.1.6"]
                 [lib-noir "0.7.4"]
                 [jarohen/nomad "0.5.1"]
                 [com.novemberain/pantomime "2.0.0"]
                 [ch.qos.logback/logback-core "1.0.13"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.jclouds/jclouds-allblobstore "1.6.0"]
                 [org.postgresql/postgresql "9.2-1003-jdbc4"]
                 [ragtime "0.3.4"]
                 [clj-time "0.6.0"]
                 [org.clojars.ato/clojure-jsr223 "1.5.1"]
                 [org.codehaus.groovy/groovy "2.1.9"]
                 [org.codehaus.groovy/groovy-xml "2.1.9"]
                 [org.codehaus.groovy/groovy-sql "2.1.9"]
                 [org.codehaus.groovy/groovy-jsr223 "2.1.9"]
                 [org.bouncycastle/bcpkix-jdk15on "1.49"]
                 [org.bouncycastle/bcprov-jdk15on "1.49"]
                 [commons-codec "1.8"]]
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
