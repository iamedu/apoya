(ns apoya.services.upload
  (:require [apoya.topics :as t]
            [apoya.util.log :as log]
            [cljs.reader :as reader]
            [shoreleave.browser.cookies :as ck]))

(defn watch-uploads [id & {:keys [on-change]}]
  (let [el (.getElementById js/document id)]
    (-> js/FileAPI (.-event) (.on el "change" on-change))))

(defn event-files [evt]
  (-> js/FileAPI (.getFiles evt)))

(defn wrap-complete [url complete-fn]
  (fn [evt {:keys [status] :as xhr}]
    (condp = status
      200 (if complete-fn (complete-fn evt xhr))
      (t/publish :error {:status status
                         :body (-> (:response xhr) reader/read-string)
                         :route {:method "POST"
                                 :uri url}}))))

(defn upload-files [url files & {:keys [progress complete]}]
  (let [params {:url url
                :headers {:x-csrf-token (:__anti-forgery-token ck/cookies)}
                :progress progress
                :complete (wrap-complete url complete)
                :files {:files files}}]
    (.upload js/FileAPI (clj->js params))))

