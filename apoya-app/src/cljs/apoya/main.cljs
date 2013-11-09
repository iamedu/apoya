(ns apoya.main
  (:require [apoya.remote.request :as r]))

(.log js/console "Hola mundo")

(defn bind-persona []
  (let [button (js/jQuery "#persona")]
    (-> js/navigator .-id
        (.watch (clj->js {:onlogin (fn [assertion]
                                     (r/edn [:post "/api/public/v1/auth/persona-login.edn"]
                                            :content {:assertion assertion}))
                          :onlogout (fn [])})))
    (.click button
            (fn []
              (.log js/console "Button clicked")
              (-> js/navigator .-id .request)))))

(r/edn [:post "/api/public/v1/auth/login.edn"]
       :content {:username "iamedu"
                 :password "password"})

(r/edn [:post "/hola/pruebas.edn"]
       :content {:hola "adios"}
       :on-success (fn [result]
                     (.log js/console (clj->js result)))
       :on-error (fn [result]
                   (.log js/console (clj->js result))))

(bind-persona)
