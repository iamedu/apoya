(ns apoya.services.scripting
  (:require [apoya.errors :as errors]
            [fortress.util :as futil])
  (:import [javax.script ScriptEngineManager]
           [java.io StringWriter PrintWriter]
           [java.util Date]))

(defonce sessions (atom {}))

(defn get-nspace [engine-name engine]
  (condp = engine-name
    "clojure" (.eval engine "(.toString clojure.core/*ns*)")
    engine-name))

(defn- prepare-session [engine-name engine]
  (let [uuid (futil/random-uuid-str)]
    {:uuid uuid
     :nspace (get-nspace engine-name engine)
     :engine-name engine-name
     :creation-date (Date.)
     :last-used (Date.)
     :engine engine}))

(defn create-session [engine-name]
  (let [engine (-> (ScriptEngineManager.) (.getEngineByName engine-name))
        session (prepare-session engine-name engine)]
    (swap! sessions assoc (:uuid session) session)
    session))

(defn touch-session [uuid]
  (swap! sessions assoc-in [uuid :last-used] (Date.)))

(defn prepare-result [res]
  (cond
    (nil? res) "nil"
    :else (pr-str res)))

(defn eval-line [uuid line]
  (let [session (get @sessions uuid)
        {:keys [engine engine-name]} session
        std-output-str (StringWriter.)
        err-output-str (StringWriter.)
        std-writer (PrintWriter. std-output-str)
        err-writer (PrintWriter. err-output-str)
        nspace (get-nspace engine-name engine)]
    (touch-session uuid)
    (-> engine (.getContext) (.setWriter std-writer))
    (-> engine (.getContext) (.setErrorWriter err-writer))
    (try
      {:result (prepare-result (.eval engine line)) :output (.toString std-output-str) :nspace nspace :error-output (.toString err-output-str)}
      (catch Exception e
        {:exception (errors/str-throwable e) :exception-message (.getMessage e)
         :output (.toString std-output-str) :nspace nspace
         :error-output (.toString err-output-str)}))))

(defn close-session [uuid]
  (let [{:keys [engine-name nspace]} (get @sessions uuid)]
    (swap! sessions dissoc uuid)))

(defn close-old-sessions [date seconds]
  (let [old-session? #(> (- (.getTime date) (.getTime (:last-used %))) (* seconds 1000))]
    (doseq [[_ session] @sessions]
      (if (old-session? session)
        (close-session (:uuid session))))))

(defn list-engines []
  (->> (ScriptEngineManager.)
       (.getEngineFactories)
       (map #(first (.getNames %)))
       (sort)))

(defn eval-code [engine-name code & {:as variables}]
  (let [engine (-> (ScriptEngineManager.) (.getEngineByName engine-name))
        std-output-str (StringWriter.)
        err-output-str (StringWriter.)
        std-writer (PrintWriter. std-output-str)
        err-writer (PrintWriter. err-output-str)
        exception (atom nil)]
    (-> engine (.getContext) (.setWriter std-writer))
    (-> engine (.getContext) (.setErrorWriter err-writer))
    (try
      (.eval engine code)
      (catch Exception e
        (reset! exception (errors/str-throwable e))))
    {:output (.toString std-output-str)
     :error-output (.toString err-output-str)
     :exception @exception}))

