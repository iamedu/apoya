(ns apoya.data.auth
  (:require [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema))

(defn find-user-any [criterion]
  (first (select users
                 (with roles
                   (fields :role_code :description))
                 (where (and (= :active true)
                             (or {:username criterion}
                                 {:email criterion})))
                 (limit 1))))

(defn find-user [& {:as criteria}]
  (first (select users
                 (with roles
                   (fields :role_code :description))
                 (where (merge criteria {:active true}))
                 (limit 1))))

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

(defn has-any-permission? [permissions & {:as criteria}]
  (let [user-criteria (assoc criteria
                             :person_permissions.permission ['in permissions])
        not-empty? (complement empty?)]
    (-> (union*)
        (queries (subselect users
                            (fields :person_permissions.permission)
                            (join :person_permissions (= :person_permissions.username :username))
                            (where user-criteria))
                 (subselect roles
                            (fields :role_permissions.permission)
                            (join :role_assignments (= :role_assignments.role_code :role_code))
                            (join :role_permissions (= :role_permissions.role_code :role_code))
                            (where {:role_assignments.username [in (subselect users
                                                                              (fields :username)
                                                                              (where criteria))]
                                    :role_permissions.permission [in permissions]})))
        (exec)
        (not-empty?))))

(defn impersonate-permission-exists? [impersonate-username current-date & {:as criteria}]
  (let [current-date (java.sql.Date. (.getTime current-date))
        not-empty? (complement empty?)]
    (-> (select impersonate_permissions
                (where {:impersonated_username impersonate-username
                        :valid_from_date [<= current-date]
                        :valid_to_date [>= current-date]
                        :permitted_username [in (subselect users
                                                           (fields :username)
                                                           (where criteria))]}))
        (not-empty?))))

