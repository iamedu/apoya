(ns apoya.util.resources)

(def loaded-js (atom #{}))

(defn load-js [js-res & {:keys [on-ready]}]
  (let [el (.createElement js/document "script")
        body (.-body js/document)
        loaded (atom false)
        real-ready (fn []
                     (when-not @loaded 
                       (swap! loaded-js conj js-res)
                       (on-ready)
                       (reset! loaded true)))]
    (when on-ready
      (aset el "onreadystatechange" real-ready)
      (aset el "onload" real-ready))
    (aset el "src" js-res)
    (.appendChild body el)))

(defn eval-js [js-code]
  (let [el (.createElement js/document "script")
        body (.-body js/document)]
    (aset el "text" js-code)
    (.appendChild body el)))

(defn load-codemirror []
  (load-js "bower_components/codemirror/lib/codemirror.js"))

(defn load-sir-trevor [on-ready]
  (letfn [(load-trevor []
                        (load-js "bower_components/sir-trevor-js/sir-trevor.min.js"
                                 :on-ready on-ready))
          (load-eventable []
                           (load-js "bower_components/Eventable/eventable.js"
                                    :on-ready load-trevor))
          (load-underscore []
                            (load-js "bower_components/underscore/underscore-min.js"
                                     :on-ready load-eventable))]
    (load-underscore)))

(defn load-angular-spinner [page-ready]
  (when (every? @loaded-js ["bower_components/angular/angular.min.js", "bower_components/spin.js/spin.js"])
    (load-js "bower_components/angular-spinner/angular-spinner.min.js" :on-ready page-ready)))

(defn jquery-loaded [select2 page-ready]
  (load-js "bower_components/nprogress/nprogress.js" :on-ready #(.start js/NProgress)) 
  (load-js "bower_components/select2/select2.min.js" :on-ready #(do
                                                                  (load-js select2)
                                                                  (page-ready))))

(defn angular-loaded [angular page-ready]
  (load-js angular :on-ready page-ready)
  (load-angular-spinner page-ready)
  (load-js "bower_components/angular-ui-bootstrap-bower/ui-bootstrap-tpls.min.js" :on-ready page-ready)
  (load-js "bower_components/angular-ui-select2/src/select2.js" :on-ready page-ready))

(defn stop-nprogress [page-ready]
  (when (every? @loaded-js ["bower_components/jquery/jquery.min.js"
                            "bower_components/angular/angular.min.js"
                            "bower_components/store.js/store.min.js"
                            "bower_components/spin.js/spin.js"
                            "bower_components/nprogress/nprogress.js"
                            "bower_components/select2/select2.min.js"
                            "bower_components/angular-ui-bootstrap-bower/ui-bootstrap-tpls.min.js"
                            "bower_components/angular-ui-select2/src/select2.js"
                            "bower_components/angular-spinner/angular-spinner.min.js"])
    (if page-ready
      (page-ready))
    (.done js/NProgress)))

(defn load-base [page-ready]
  (let [{:strs [angular select2]} (js->clj (aget js/document "apoya_locales"))
        page-ready (partial stop-nprogress page-ready)]
    (load-js "bower_components/jquery/jquery.min.js" :on-ready (partial jquery-loaded select2 page-ready))
    (load-js "bower_components/angular/angular.min.js" :on-ready (partial angular-loaded angular page-ready))
    (load-js "bower_components/store.js/store.min.js" :on-ready page-ready)
    (load-js "bower_components/spin.js/spin.js" :on-ready (partial load-angular-spinner page-ready))))

(defn load-persona []
  (load-js "https://login.persona.org/include.js"))
