(function () {
  var startLoading, loadRest, finishedLoading;
  startLoading = function () {
    head.load("bower_components/jquery/jquery.min.js", "bower_components/nprogress/nprogress.js", function () {
      NProgress.start();
      loadRest();
    });
  }
  loadRest = function () {
    head.load("css/bootstrap.css",
              "bower_components/nprogress/nprogress.css",
              "bower_components/font-awesome/css/font-awesome.min.css",
              "bower_components/codemirror/lib/codemirror.css",
              "js/jquery.console.js",
              "bower_components/bootstrap/dist/js/bootstrap.js",
              "bower_components/store.js/store.min.js",
              "bower_components/angular/angular.min.js",
              "bower_components/angular-route/angular-route.min.js",
              "bower_components/angular-ui-bootstrap-bower/ui-bootstrap.min.js",
              "bower_components/select2/select2.min.js",
              "bower_components/angular-ui-select2/src/select2.js",
              "bower_components/spin.js/dist/spin.min.js",
              "bower_components/angular-spinner/angular-spinner.min.js",
              "bower_components/nginfinitescroll/build/ng-infinite-scroll.min.js",
              "bower_components/codemirror/lib/codemirror.js",
              "bower_components/codemirror/mode/sql/sql.js",
              "bower_components/codemirror/mode/clojure/clojure.js",
              "bower_components/codemirror/addon/edit/matchbrackets.js",
              "bower_components/angular-ui-codemirror/ui-codemirror.js",
              "bower_components/underscore/underscore-min.js",
              "bower_components/Eventable/eventable.js",
              "bower_components/sir-trevor-js/sir-trevor.min.js",
              finishedLoading);
  };
  finishedLoading = function () {
    head.load("js/main.js", function () {
      NProgress.done();
    });
  };
  startLoading();
}());
