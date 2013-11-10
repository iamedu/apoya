(ns apoya.util.angular)

(defn keystr
  "Helper fn that converts keywords into strings"
  [x]
  (if (keyword? x)
    (name x)
    x))

(defn strkey
  "Helper fn that converts keywords into strings"
  [x]
  (if (string? x)
    (keyword x)
    x))

(extend-type object
  ILookup
  (-lookup
    ([o k]
     (aget o (keystr k)))
    ([o k not-found]
     (let [s (keystr k)]
       (if (goog.object.containsKey o s)
         (aget o s)
         not-found)))))

(extend-type object
  ISeqable
  (-seq [o]
    (let [ks (goog.object.getKeys o)]
      (map #(vector (strkey %) (aget o %)) ks))))

(defn keykeys [o]
  (let [ks (goog.object.getKeys o)]
    (into {} (map #(vector (strkey %) (aget o %)) ks))))

(defn oset! [obj & kvs]
  (doseq [[k v] (partition 2 kvs)]
    (aset obj (name k) (clj->js v)))
  (when (and (not (aget obj "$$phase"))
             (not (-> obj (aget "$root") (aget "$$phase"))))
    (.$digest obj)))


