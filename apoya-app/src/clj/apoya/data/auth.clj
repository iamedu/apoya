(ns apoya.data.auth
  (:require [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema))

(defn find-user [& {:as criteria}]
  (let [user (first (select users
                            (with roles
                              (fields :role_code :description))
                            (where (merge criteria {:active true}))
                            (limit 1)))
        roles (:roles user)]
    (if (= (count roles) 1)
      (assoc user :current-role (-> roles first))
      user)))

(defn find-user-roles [& {:as criteria}]
  (set (map #(keyword (:role_code %))
            (select :role_assignments
                    (fields :role_code)
                    (where criteria)))))

(defn find-user-urls [& {:as criteria}]
  (let [user (first (select users
                            (fields :username)
                            (with restricted_urls
                              (fields :url))
                            (with roles
                              (fields :role_code))
                            (where (merge criteria {:active true}))
                            (limit 1)))
        {:keys [restricted_urls roles]} user
        roles (map :role_code roles)
        user-urls restricted_urls
        role-urls (select :role_urls
                          (fields :url)
                          (where {:role_code [in roles]}))
        urls (concat user-urls role-urls)]
    (->> urls
         (map :url)
         (distinct)
         (map re-pattern))))

(defn find-user-permissions [& {:as criteria}]
  (let [user (first (select users
                            (fields :username)
                            (with permissions
                              (fields :permission))
                            (with roles
                              (fields :role_code))
                            (where (merge criteria {:active true}))
                            (limit 1)))
        {:keys [permissions roles]} user
        roles (map :role_code roles)
        user-permissions permissions
        role-permissions (select :role_permissions
                                 (fields :permission)
                                 (where {:role_code [in roles]}))
        permissions (concat user-permissions role-permissions)]
    (->> permissions
         (map :permission)
         (set))))
