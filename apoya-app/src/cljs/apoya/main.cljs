(ns apoya.main
  (:require [apoya.repl :refer [bind-repl]]
            [shoreleave.remote :as r]))

(.log js/console "Hola mundo")

(defn bind-persona []
  (let [button (js/jQuery "#persona")]
    (-> js/navigator .-id
        (.watch (clj->js {:onlogin (fn [assertion]
                                     (r/request [:post "/api/public/v1/auth/persona-login.edn"]
                                                :content {:assertion assertion}))
                          :onlogout (fn [])})))
    (.click button
            (fn []
              (.log js/console "Button clicked")
              (-> js/navigator .-id .request)))))

;; (r/request [:post "/api/public/v1/auth/login.edn"]
;;            :content {:username "iamedu"
;;                      :password "password"})
(r/request [:get "/hola.edn"]
           :content {:username "iamedu"
                     :password "password"}
           :headers {"Accept" "application/edn; charset=utf-8"})

(bind-repl)
