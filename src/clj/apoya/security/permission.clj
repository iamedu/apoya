(ns apoya.security.permission
  (:require [apoya.data.auth :as auth]
            [clojure.string :as s])
  (:import [java.util Date]))

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

(defn has-any-permission? [permissions & {:as criteria}]
  (let [criteria (-> criteria seq flatten)]
    (boolean (some #(apply has-permission? % criteria) permissions))))

(defn can-impersonate? [impersonate-username & {:keys [current-date]
                                               :or {current-date (Date.)}
                                               :as criteria}]
  (let [criteria (-> (dissoc criteria :current-date) seq flatten)]
    (or (and (apply has-permission? "user:impersonate-any" criteria)
             (not (nil? (auth/find-user :username impersonate-username))))
        (apply auth/impersonate-permission-exists? impersonate-username current-date criteria))))

