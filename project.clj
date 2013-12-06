(defproject apoya "0.0.2-SNAPSHOT"
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
                 [ring/ring-core "1.2.1"]
                 [org.clojars.iamedu/fortress-ring-adapter "0.1.0-SNAPSHOT"]
                 [compojure "1.1.6"]
                 [lib-noir "0.7.6"]
                 [bk/ring-gzip "0.1.1"]
                 [ring-anti-forgery "0.3.0"]
                 [com.cemerick/friend "0.2.0"]
                 [liberator "0.9.0"]
                 ;; Clojure libs
                 [com.novemberain/pantomime "2.0.0"]
                 [jarohen/nomad "0.5.1"]
                 [environ "0.4.0"]
                 [korma "0.3.0-RC6"]
                 [cheshire "5.2.0"]
                 [ragtime "0.3.4"]
                 [markdown-clj "0.9.35"]
                 [clj-time "0.6.0"]
                 [clojure-watch "LATEST"]
                 [fleet "0.10.1"]
                 [clj-pdf "1.11.6"
                  :exclusions [org.bouncycastle/bctsp-jdk14]]
                 [clj-http "0.7.7"]
                 [clojurewerkz/elastisch "1.3.0-beta5"]
                 [com.draines/postal "1.11.1"]
                 [de.ubercode.clostache/clostache "1.3.1"]
                 [pallet-fsm "0.2.0"]
                 [xmpp-clj "0.3.1"]
                 [clojurewerkz/quartzite "1.1.0"]
                 [clj-time "0.6.0"]
                 [org.clojure/tools.reader "0.7.10"]
                 [com.novemberain/langohr "1.7.0"]
                 [clj-stacktrace "0.2.7"]
                 ;; Java libs
                 [ch.qos.logback/logback-core "1.0.13"]
                 [ch.qos.logback/logback-classic "1.0.13"]
                 [org.apache.jclouds/jclouds-allblobstore "1.6.3"]
                 [org.postgresql/postgresql "9.3-1100-jdbc41"]
                 [org.clojars.iamedu/clojure-jsr223 "1.5.1-SNAPSHOT"]
                 [org.codehaus.groovy/groovy "2.1.9"]
                 [org.codehaus.groovy/groovy-xml "2.1.9"]
                 [org.codehaus.groovy/groovy-sql "2.1.9"]
                 [org.codehaus.groovy/groovy-jsr223 "2.1.9"]
                 [org.bouncycastle/bcpkix-jdk15on "1.49"]
                 [org.bouncycastle/bcprov-jdk15on "1.49"]
                 [com.google.inject/guice "3.0"]
                 [commons-codec "1.8"]
                 [org.ocpsoft.prettytime/prettytime "3.2.1.Final"]
                 [org.apache.poi/poi "3.9"]
                 [org.mortbay.jetty.npn/npn-boot "1.1.6.v20130911"]
                 ;;Clojurescript libs
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [shoreleave/shoreleave-pubsub "0.3.0"]
                 [clavatar-js "0.1.0-SNAPSHOT"]]
  :source-paths ["src/clj"]
  :libdir-path "deps"
  :plugins [[lein-cljsbuild "1.0.0"]
            [lein-libdir "0.1.1"]
            [ragtime/ragtime.lein "0.3.4"]
            [lein-morecss "0.1.0-SNAPSHOT"]
            [apoya-minify "0.1.0-SNAPSHOT"]]
  :morecss {:default {:less-file "data/fs/sites/default/less/bootstrap.less"
                      :css-file "data/fs/sites/default/css/bootstrap.css"
                      :directories ["data/fs/sites/default/less"]}}
  :minify {:init-file "data/fs/sites/default/js/init.js"
           :html-file "data/fs/sites/default/index.fleet"
           :source-dir "data/fs/sites/default"
           :output-dir "app-dist/sites/default"}
  :cljsbuild {:crossovers [apoya.angular]
              :crossover-path "src/crossover-cljs"
              :builds {:main {:source-paths ["src/cljs"]
                              :compiler {:output-to "data/fs/sites/default/js/main.js"
                                         :externs  ["externs/angular.js"
                                                    "externs/persona.js"
                                                    "externs/jquery.js"
                                                    "externs/store.js"
                                                    "externs/ace.js"
                                                    "externs/nprogress.js"
                                                    "externs/fileapi.js"]
                                         :optimizations :advanced
                                         :pretty-print false}}}}
  :ragtime  {:migrations ragtime.sql.files/migrations
             :database "jdbc:postgresql://localhost:5432/apoya?user=apoya&password=apoya"}
  :aot [sun.net.www.protocol.jclouds.connection 
        sun.net.www.protocol.jclouds.handler
        apoya.util.classloader
        apoya.services.schedule
        apoya.core]
  :main apoya.core
  :target-path "target/%s"
  :jvm-opts ["-Xbootclasspath/p:lib/npn-boot-1.1.6.v20130911.jar"]
  :profiles {:uberjar {:aot :all}}
  :min-lein-version "2.0.0")
