(ns apoya.services.scripting
  (:require [apoya.errors :as errors])
  (:import [javax.script ScriptEngineManager]
           [java.io StringWriter PrintWriter]))

(defn list-engines []
  (->> (ScriptEngineManager.)
      (.getEngineFactories)
      (map #(first (.getNames %)))))

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

