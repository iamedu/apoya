(ns apoya.devel.repl
  (:require [apoya.util.resources :as r]))

(defn bind-repl []
  (.write js/document "<script src=\"repl/out/goog/base.js\"></script>")
  (.write js/document "<script src=\"repl/piggieback_browser.js\"></script>")
  (.write js/document "<script>goog.require('piggieback_browser')</script>"))

