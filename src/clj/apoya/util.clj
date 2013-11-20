(ns apoya.util
  (:require [apoya.config :as cfg])
  (:import [org.ocpsoft.prettytime PrettyTime]
           [java.util Locale Date]))

(defn pretty-from
  ([date] (pretty-from date cfg/*language*))
  ([date locale]
   (let [locale (-> locale name (Locale.))
         p (PrettyTime. locale)]
     (.format p date))))

(defn pretty-from-millis
  ([millis] (pretty-from-millis millis cfg/*language*))
  ([millis locale]
   (let [date (Date. millis)]
     (pretty-from date locale))))

