; DO NOT EDIT THIS FILE! IT WAS AUTOMATICALLY GENERATED BY
; lein-cljsbuild FROM THE FOLLOWING SOURCE FILE:
; file:/home/iamedu/Development/iamedu/apoya/apoya-app/src/crossover-cljs/apoya/angular.clj

(ns apoya.angular)

  (defmacro defangular [op module nm args body]
     `(let [str-args# (apply vector (map name (quote ~args)))
                str-name# (name (quote ~nm))
                         func# (fn ~args ~@body)
                                  full-args# (conj str-args# func#)]
                                       (~op ~module str-name# (to-array full-args#))))

  (defmacro defcontroller [module nm args & body]
     `(defangular .controller ~module ~nm ~args ~body))

  (defmacro defservice [module nm args & body]
     `(defangular .factory ~module ~nm ~args ~body))

  (defmacro defdirective [module nm args & body]
     `(defangular .directive ~module ~nm ~args ~body))
