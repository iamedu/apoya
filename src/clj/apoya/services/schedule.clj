(ns apoya.services.schedule
  (:require [apoya.data.sessions :as sessions]
            [apoya.config :as cfg]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.triggers :as t]
            [clojurewerkz.quartzite.jobs :refer [defjob] :as j] 
            [clojurewerkz.quartzite.schedule.cron :refer [schedule cron-schedule]]
            [clojure.tools.logging :as log])
  (:import [org.quartz.impl StdSchedulerFactory]
           [java.util Properties]))

(defonce scheduler (atom nil))


(defjob CloseTransactionsJob
  [ctx]
  (log/info "Closing transactions not used in 30 minutes")
  (sessions/close-old-sessions (java.util.Date.) (* 30 60)))

(def job-types
  {:close-transactions CloseTransactionsJob})

(defn schedule-job [k job-type cron & ctx]
  (let [job-type (job-type job-types)
        jk (j/key (str k ".job"))
        job (j/build
              (j/of-type job-type)
              (j/with-identity jk))
        tk (t/key k)
        trigger (t/build
                  (t/with-identity tk)
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (cron-schedule cron))))]
    (qs/schedule job trigger)))

(defn unschedule-job [k]
  (qs/unschedule-job (t/key k)))

(defn setup-scheduler [c]
  (log/info "Setting up quartz scheduler")
  (let [{:keys [classname subprotocol subname user password quartz-delegate]} (or (cfg/db-env-config c)
                                                                                  c)
        user (or user "")
        password (or password "")
        url (str "jdbc:" subprotocol ":" subname)
        props (doto (Properties.)
                (.setProperty "org.quartz.jobStore.class" "org.quartz.impl.jdbcjobstore.JobStoreTX")
                (.setProperty "org.quartz.jobStore.tablePrefix" "QRTZ_")
                (.setProperty "org.quartz.jobStore.driverDelegateClass" quartz-delegate)
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
    (reset! scheduler (qs/initialize instance))
    (qs/start)))

