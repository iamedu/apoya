(ns apoya.data.mail
  (:require [clojure.tools.logging :as log])
  (:use korma.core
        apoya.data.schema))

(defn list-base-mails [l o domain language]
  (select base_mail_templates
          (where {:domain (name domain)
                  :language (name language)})
          (order :base_key :ASC)
          (limit l)
          (offset o)))
