(ns apoya.services.schedule
  (:require [clojurewerkz.quartzite.scheduler :as sch]
            [clojure.tools.logging :as log])
  (:import [org.quartz.impl StdSchedulerFactory]
           [java.util Properties]))

(defonce scheduler (atom nil))

(defn setup-scheduler [cfg]
  (log/info "Setting up quartz scheduler")
  (let [{:keys [classname subprotocol subname user password]} cfg
        user (or user "")
        password (or password "")
        url (str "jdbc:" subprotocol ":" subname)
        props (doto (Properties.)
                (.setProperty "org.quartz.jobStore.class" "org.quartz.impl.jdbcjobstore.JobStoreTX")
                (.setProperty "org.quartz.jobStore.tablePrefix" "QRTZ_")
                (.setProperty "org.quartz.jobStore.driverDelegateClass" "org.quartz.impl.jdbcjobstore.StdJDBCDelegate")
                (.setProperty "org.quartz.jobStore.dataSource" "quartzDataSource")
                (.setProperty "org.quartz.dataSource.quartzDataSource.driver" classname)
                (.setProperty "org.quartz.dataSource.quartzDataSource.URL" url)
                (.setProperty "org.quartz.dataSource.quartzDataSource.user" user)
                (.setProperty "org.quartz.dataSource.quartzDataSource.password" password)
                (.setProperty "org.quartz.dataSource.quartzDataSource.maxConnections" "8") 
                (.setProperty "org.quartz.threadPool.threadCount" "4"))
        factory (doto (StdSchedulerFactory. props)
                  (.initialize))
        instance (.getScheduler factory)]
    (reset! scheduler (sch/initialize instance))
    (sch/start)))

