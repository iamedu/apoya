(ns apoya.topics
  (:require [shoreleave.pubsubs.publishable :as publishable]
            [shoreleave.pubsubs.simple :as pubsub]
            [goog.pubsub.PubSub :as ps]
            [shoreleave.pubsubs.protocols :as protocols]))

(def *default-bus* (pubsub/bus))

(defn subscribe [topic handler]
  (protocols/subscribe *default-bus* topic handler))

(defn publish [topic data]
  (protocols/publish *default-bus* topic data))

(defn clear-topic [topic]
  (.clear *default-bus* topic))
