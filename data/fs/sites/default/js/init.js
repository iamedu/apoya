(function () {
  var startLoading, loadRest, finishedLoading;
  startLoading = function () {
    window.FileAPI = { staticPath: 'bower_components/FileAPI/' };
    head.load("bower_components/jquery/jquery.min.js", "bower_components/nprogress/nprogress.js", function () {
      NProgress.start();
      loadRest();
    });
  }
  loadRest = function () {
    head.load("bower_components/nprogress/nprogress.css",
              "bower_components/font-awesome/css/font-awesome.min.css",
              "js/jquery.console.js",
              "bower_components/bootstrap/dist/js/bootstrap.js",
              "bower_components/store.js/store.min.js",
              "bower_components/ace-builds/src-min-noconflict/ace.js",
              "bower_components/angular/angular.min.js",
              "bower_components/angular-route/angular-route.min.js",
              "bower_components/angular-ui-bootstrap-bower/ui-bootstrap.min.js",
              "bower_components/angular-ui-ace/ui-ace.js",
              "bower_components/select2/select2.min.js",
              "bower_components/angular-ui-select2/src/select2.js",
              "bower_components/spin.js/dist/spin.min.js",
              "bower_components/angular-spinner/angular-spinner.min.js",
              "bower_components/nginfinitescroll/build/ng-infinite-scroll.min.js",
              "bower_components/underscore/underscore-min.js",
              "bower_components/Eventable/eventable.js",
              "bower_components/sir-trevor-js/sir-trevor.min.js",
              "bower_components/keymaster/keymaster.js",
              "bower_components/FileAPI/FileAPI.min.js",
              finishedLoading);
  };
  finishedLoading = function () {
    head.load("js/main.js", function () {
      NProgress.done();
    });
  };
  startLoading();
}());
