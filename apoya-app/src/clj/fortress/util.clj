(ns fortress.util
  (:import [java.awt GraphicsEnvironment]
           [javax.swing JPasswordField JOptionPane]
           [java.util UUID]))

(defn random-uuid-str []
  (.toString (UUID/randomUUID)))

(defn read-password []
  (cond
    (not (GraphicsEnvironment/isHeadless)) (let [pf (JPasswordField.)
                                                 result (JOptionPane/showConfirmDialog nil pf
                                                                                       "Fortress: Enter certificate password"
                                                                                       JOptionPane/OK_CANCEL_OPTION
                                                                                       JOptionPane/PLAIN_MESSAGE)]
                                             (if (= result JOptionPane/OK_OPTION)
                                               (.getPassword pf)))
    (not (nil? (System/console))) (.readPassword (System/console) "Private key password: " (object-array 0))))

