(ns apoya.main
  (:require [apoya.remote.request :as r]
            [apoya.util.resources :refer [load-base load-piggie-repl]]))

(load-base #(.done js/NProgress))
