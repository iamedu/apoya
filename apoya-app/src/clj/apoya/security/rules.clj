(ns apoya.security.rules)

(defn check-access [request]
  true)

(defn access-forbidden [request]
  {:body "NO PUEDES!"})
