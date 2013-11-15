(ns apoya.resources.optimize
  (:require [ring.util.response :as response]
            [clojure.string :as s]))

(defn links [resp & resources]
  (let [link-strs (for [[f p] resources]
                    (str "<" f ">; rel="
                         (name (or p :prefetch))))
        link-str (s/join "," link-strs)]
    (response/header resp "Link" link-str)))

(defn modern-ie [resp]
  (response/header resp "X-UA-Compatible" "IE=Edge"))

(defn wrap-modern-ie [handler]
  (fn [request]
    (if-let [resp (handler request)]
      (modern-ie resp))))
