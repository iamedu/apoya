(ns apoya.resources.optimize
  (:require [ring.util.response :as response]
            [clojure.string :as s]
            ))

(defn links [resp resources]
  (let [link-strs (for [[f p] resources]
                   (str "<" f ">; rel="
                        (name (or p :prefetch))))
        link-str (s/join "," link-strs)]
    (response/header
      resp
      "Link"
      link-str)))
