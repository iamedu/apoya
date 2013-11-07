(ns apoya.main
  (:require [shoreleave.remote :as r]))

(.log js/console "Hola mundo")

(r/request [:post "/api/v1/auth/login.edn"]
           :content {:username "iamedu"
                     :password "password"})
