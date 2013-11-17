(ns apoya.i18n
  (:require [apoya.config :as cfg]
            [clojure.string :as s]
            [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema))

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

(defn label [k & {:as options}]
  (let [domain (name cfg/*current-site*)
        language (name cfg/*language*)
        str-k (name k)
        default-text (or (:default options) str-k) 
        params (:params options)
        db-label (or (first (select labels
                                    (where {:label_key str-k
                                            :language language
                                            :domain domain})))
                     (insert labels
                             (values {:label_key str-k
                                      :language language
                                      :domain domain
                                      :label_text default-text})))]
    (apply format (:label_text db-label) params)))
