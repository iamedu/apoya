(ns apoya.main
  (:require-macros [apoya.angular :refer [defcontroller]])
  (:require [apoya.remote.request :as r]
            [apoya.util.resources :refer [load-base load-piggie-repl]]))

(load-base #(.log js/console "Done loading"))
