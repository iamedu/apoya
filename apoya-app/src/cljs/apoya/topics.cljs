(ns apoya.topics
  (:require [shoreleave.pubsubs.publishable :as publishable]
            [shoreleave.pubsubs.event :as event]
            [shoreleave.pubsubs.protocols :as protocols]))

(def *default-bus* (event/bus))

(defn subscribe [topic handler]
  (protocols/subscribe *default-bus* topic handler))

(defn publish [topic data]
  (protocols/publish *default-bus* topic data))

