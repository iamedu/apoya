(ns fortress.util
  (:require [clojure.string :as s])
  (:import [java.awt GraphicsEnvironment]
           [javax.swing JPasswordField JOptionPane]
           [java.security MessageDigest]  
           [java.util UUID]))

(defn sha1
  "Generates a SHA-1 hash of the given input plaintext."
  [input]
  (let [md  (MessageDigest/getInstance "SHA-1")]
    (. md update (.getBytes input))
    (let  [digest (.digest md)]
      (s/join "" (map #(Integer/toHexString  (bit-and % 0xff)) digest)))))

(defn random-uuid-str []
  (.toString (UUID/randomUUID)))

(defn random-sha1 []
  (sha1 (random-uuid-str)))

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

