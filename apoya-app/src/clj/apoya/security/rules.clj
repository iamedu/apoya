(ns apoya.security.rules
  (:require [apoya.data.auth :as auth]
            [apoya.data.site :as site]
            [cemerick.friend :as friend]))

(defn check-access [request]
  (let [uri (:uri request)
        id (:current (friend/identity request))
        urls (auth/find-user-urls :username id)
        access? (boolean (some #(re-matches % uri) urls))]
    (if (and (not access?)
             (not (site/url-exists? uri)))
      (site/create-url {:url uri}))
    access?))
