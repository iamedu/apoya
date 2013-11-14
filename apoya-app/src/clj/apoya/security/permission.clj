(ns apoya.security.permission
  (:require [apoya.data.auth :as auth]
            [clojure.string :as s]))

(defn- permission-alternatives [permission]
  (let [parts (s/split permission #":")
        reduce-fn (fn [acc part]
                    (for [alt ["*" part]
                          alt-list acc]
                      (do
                        (conj alt-list alt))))
        alt-lists (reduce reduce-fn [[]] parts)]
    (for [alt-list alt-lists]
      (s/join ":" alt-list))))

(defn has-permission? [permission & {:as criteria}]
  (let [permission-alts (permission-alternatives permission)
        criteria (-> criteria seq flatten)]
    (apply auth/has-any-permission? permission-alts criteria)))
