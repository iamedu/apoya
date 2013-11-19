(ns apoya.services.fs
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [apoya.remote.request :as r]))

(defn list-contents [folder]
  (r/edn [:post "/api/v1/fs/list-contents.edn"]
         :content {:folder folder}))

