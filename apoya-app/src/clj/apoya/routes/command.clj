(ns apoya.routes.command
  (:use compojure.core)
  (:require [apoya.response :as r]
            [apoya.data.sessions :as sess]))

;;; Sql
(defroutes sql-routes
  (POST "/create-session.edn" request
        (let [sess (sess/create-session)]
          (r/edn-response (dissoc sess :conn)))))

(defroutes command-routes
  (context "/sql" [] sql-routes))
