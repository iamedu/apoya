(ns apoya.main
  (:require-macros [apoya.angular :refer [defcontroller]])
  (:require [apoya.remote.request :as r]
            [apoya.util.resources :refer [load-base load-piggie-repl]]
            [apoya.util.log :as log]))

(def *default-output* (log/console-output))
(log/start-display *default-output*)

(load-base #(log/info "Done loading"))
