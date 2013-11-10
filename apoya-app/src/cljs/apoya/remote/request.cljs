(ns apoya.remote.request
  (:require [apoya.topics :as t]
            [cljs.reader :as reader]
            [clojure.browser.event :as event]
            [cljs.core.async :as async :refer [chan close! put!]]
            [goog.net.XhrManager :as manager]
            [shoreleave.remotes.common :as common]))

(def ^:private responders (atom {}))

(defn- add-responders [id success error]
  (when (or success error)
    (swap! responders assoc id {:success success :error error})))

(extend-type goog.net.XhrManager

  event/EventType
  (event-types [this]
    (into {}
          (map
            (fn [[k v]]
              [(keyword (. k (toLowerCase)))
               v])
            (js->clj goog.net.EventType)))))

(def ^:private
  *xhr-manager*
  (goog.net.XhrManager. nil
                        nil
                        nil
                        0
                        5000))

(defn edn
  [route & {:keys [id content headers priority retries]
            :or   {id (common/rand-id-str), retries 0}}]
  (let [rc (chan 1)
        on-success (fn [result]
                     (put! rc
                           (update-in result [:body] cljs.reader/read-string)
                           #(close! rc)))
        on-error (fn [error]
                   (let [error (update-in error [:body] cljs.reader/read-string)]
                     (t/publish :error error)
                     (put! rc
                           #(close! rc))))
        [method uri] (common/parse-route route)]
    (try
      (add-responders id on-success on-error)
      (.send *xhr-manager*
             id
             uri
             method
             (pr-str (common/csrf-protected (or content {}) method))
             (clj->js (assoc headers
                             "Content-Type" "application/edn; charset=UTF-8"
                             "Accept" "application/edn; charset=UTF-8"))
             priority
             ;; This next one is a callback, and we could use it to get
             ;; rid of the atom and figure out success/failure ourselves
             nil
             retries)
      (catch js/Error e
        nil))
    rc))

(defn- response-success [e]
  (when-let [{success :success} (get @responders (:id e))]
    (success e)
    (swap! responders dissoc (:id e))))

(defn- response-error [e]
  (when-let [{error :error} (get @responders (:id e))]
    (error e)
    (swap! responders dissoc (:id e))))

(defn- response-received
  [f e]
  (f {:id     (.-id e)
      :body   (.getResponse e/xhrIo)
      :status (.getStatus e/xhrIo)
      :outcome (if (= 200 (.getStatus e/xhrIo))
                 :ok
                 :error)
      :event  e}))

(event/listen *xhr-manager* "success" (partial response-received response-success))
(event/listen *xhr-manager* "error"   (partial response-received response-error))
