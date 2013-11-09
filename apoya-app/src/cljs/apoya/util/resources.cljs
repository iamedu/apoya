(ns apoya.util.resources)

(defn load-js [js-res & {:keys [on-ready]}]
  (let [el (.createElement js/document "script")
        body (.-body js/document)
        loaded (atom false)
        real-ready (fn []
                     (when-not @loaded
                       (on-ready)
                       (reset! loaded true)))]
    (when on-ready
      (aset el "onreadystatechange" on-ready)
      (aset el "onload" on-ready))
    (aset el "src" js-res)
    (.appendChild body el)))

(defn eval-js [js-code]
  (let [el (.createElement js/document "script")
        body (.-body js/document)]
    (aset el "text" js-code)
    (.appendChild body el)))

(defn load-codemirror []
  (load-js "bower_components/codemirror/lib/codemirror.js"))

(defn load-sir-trevor []
  (load-js "bower_components/underscore/underscore-min.js")
  (load-js "bower_components/Eventable/eventable.js")
  (load-js "bower_components/sir-trevor-js/sir-trevor.min.js"))

(defn load-base [page-ready]
  (let [{:strs [angular select2]} (js->clj (aget js/document "apoya_locales"))]
    (load-js "bower_components/jquery/jquery.min.js")
    (load-js "bower_components/nprogress/nprogress.js" :on-ready #(.start js/NProgress))
    (load-js "bower_components/angular/angular.min.js" :on-ready #(load-js angular))
    (load-js "bower_components/select2/select2.min.js" :on-ready #(load-js select2))
    (load-js "bower_components/store.js/store.min.js")
    (load-js "bower_components/spin.js/spin.js")
    (load-js "bower_components/angular-spinner/angular-spinner.min.js")
    (load-js "bower_components/angular-ui-bootstrap-bower/ui-bootstrap-tpls.min.js")
    (load-js "bower_components/angular-ui-select2/src/select2.js" :on-ready #(page-ready))))

(defn load-persona []
  (load-js "https://login.persona.org/include.js"))
