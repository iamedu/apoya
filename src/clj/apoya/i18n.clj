(ns apoya.i18n
  (:require [apoya.config :as cfg]
            [clojure.string :as s]
            [clojure.core.cache :as cache]
            [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema))

(defonce labels-cache (atom (cache/ttl-cache-factory {} :ttl (* 10 60 1000))))

(defn available-languages []
  (set (map #(-> % :language keyword)
            (select languages))))

(defn find-supported-language [{:keys [headers]}]
  (let [default-language cfg/*language*
        site-languages (available-languages)
        wanted-languages (-> (get headers "accept-language" (name default-language))
                             (s/split #","))
        wanted-languages (map #(if (re-find #";q=.+" %)
                                 (let [[_ e q] (re-find #"([\w-]+);q=(.+)" %)]
                                   {:encoding (keyword e) :qvalue (Float/parseFloat q)})
                                 {:encoding (keyword %) :qvalue 1.0})
                              wanted-languages)
        wanted-languages (sort-by :qvalue > wanted-languages)
        wanted-languages (map :encoding wanted-languages)]
    (or (some site-languages wanted-languages)
        default-language)))

(defn- cached-label [full-key get-label]
  (let [new-cache (if (cache/has? @labels-cache full-key)
                    (cache/hit @labels-cache full-key)
                    (swap! labels-cache cache/miss full-key (get-label)))]
    (get new-cache full-key)))

(defn label [k & {:as options}]
  (let [domain (name cfg/*current-site*)
        language (name cfg/*language*)
        str-k (name k)
        default-text (or (:default options) str-k) 
        params (:params options)
        full-key {:label_key str-k
                  :language language
                  :domain domain}
        get-label #(or (first (select labels
                                      (where full-key)))
                       (insert labels
                               (values {:label_key str-k
                                        :language language
                                        :domain domain
                                        :label_text default-text})))
        db-label (cached-label full-key get-label)]
    (apply format (:label_text db-label) params)))
