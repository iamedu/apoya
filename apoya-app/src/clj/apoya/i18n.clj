(ns apoya.i18n)

(defn label [k & {:as options}]
  (or (:default options)
      (name k)))
