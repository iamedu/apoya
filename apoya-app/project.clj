(defproject apoya "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2014"]
                 [org.clojure/core.cache "0.6.3"]
                 [org.clojure/core.async "0.1.242.0-44b1e3-alpha"]
                 [org.clojure/tools.cli "0.2.4"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.clojure/java.jmx "0.2.0"]
                 [com.cemerick/piggieback "0.1.2"]
                 ;; Ring libs
                 [ring/ring-core "1.2.0"]
                 [org.clojars.iamedu/fortress-ring-adapter "0.1.0-SNAPSHOT"]
                 [compojure "1.1.6"]
                 [lib-noir "0.7.5"]
                 [bk/ring-gzip "0.1.1"]
                 [ring-anti-forgery "0.3.0"]
                 [com.cemerick/friend "0.2.0"]
                 [liberator "0.9.0"]
                 ;; Clojure libs
                 [com.novemberain/pantomime "2.0.0"]
                 [jarohen/nomad "0.5.1"]
                 [korma "0.3.0-RC6"]
                 [cheshire "5.2.0"]
                 [ragtime "0.3.4"]
                 [clj-time "0.6.0"]
                 [clojure-watch "LATEST"]
                 [fleet "0.10.1"]
                 [clj-pdf "1.11.6"
                  :exclusions [org.bouncycastle/bctsp-jdk14]]
                 [clj-http "0.7.7"]
                 [clojurewerkz/elastisch "1.3.0-beta5"]
                 [com.draines/postal "1.11.1"]
                 [pallet-fsm "0.2.0"]
                 ;; Java libs
                 [ch.qos.logback/logback-core "1.0.13"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.jclouds/jclouds-allblobstore "1.6.0"]
                 [org.postgresql/postgresql "9.3-1100-jdbc41"]
                 [com.asual.lesscss/lesscss-engine "1.4.2"] 
                 [org.clojars.ato/clojure-jsr223 "1.5.1"]
                 [org.codehaus.groovy/groovy "2.1.9"]
                 [org.codehaus.groovy/groovy-xml "2.1.9"]
                 [org.codehaus.groovy/groovy-sql "2.1.9"]
                 [org.codehaus.groovy/groovy-jsr223 "2.1.9"]
                 [org.bouncycastle/bcpkix-jdk15on "1.49"]
                 [org.bouncycastle/bcprov-jdk15on "1.49"]
                 [com.google.inject/guice "3.0"]
                 [commons-codec "1.8"]
                 [org.clojure/tools.reader "0.7.10"]
                 [org.apache.activemq/activemq-broker "5.9.0"]
                 [net.sf.ehcache/ehcache "2.7.4"]
                 ;;Clojurescript libs
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [shoreleave/shoreleave-pubsub "0.3.0"]
                 [clavatar "0.2.1"]]
  :source-paths ["src/clj"]
  :libdir-path "deps"
  :plugins [[lein-cljsbuild "1.0.0-alpha2"]
            [lein-libdir "0.1.1"]
            [ragtime/ragtime.lein "0.3.4"]]
  :cljsbuild {:crossovers [apoya.angular]
              :crossover-path "src/crossover-cljs"
              :builds {:main {:source-paths ["src/cljs"]
                              :compiler {:output-to "data/fs/sites/default/js/main.js"
                                         :externs  ["externs/angular.js"
                                                    "externs/persona.js"
                                                    "externs/nprogress.js"]
                                         :optimizations :advanced
                                         :pretty-print false}}}}
  :ragtime  {:migrations ragtime.sql.files/migrations
             :database "jdbc:postgresql:apoya"}
  :aot [sun.net.www.protocol.jclouds.connection 
        sun.net.www.protocol.jclouds.handler
        apoya.util.classloader]
  :main ^:skip-aot apoya.core
  :target-path "target/%s"
  :jvm-opts ["-Xbootclasspath/p:lib/npn-boot-1.1.6.v20130911.jar"]
  :profiles {:uberjar {:aot :all}})
