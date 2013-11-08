(ns apoya.main
  (:require [shoreleave.remote :as r]))

(.log js/console "Hola mundo")

(let [button (js/jQuery "#persona")]
  (-> js/navigator .-id
      (.watch (clj->js {:onlogin (fn [assertion]
                                   (r/request [:post "/api/public/v1/auth/persona-login.edn"]
                                              :content {:assertion assertion}))
                        :onlogout (fn [])})))
  (.click button
          (fn []
            (.log js/console "Button clicked")
            (-> js/navigator .-id .request))))

(r/request [:post "/api/public/v1/auth/login.edn"]
           :content {:username "iamedu"
                     :password "password"})

