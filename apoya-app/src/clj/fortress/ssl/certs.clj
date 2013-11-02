(ns fortress.ssl.certs
  (:import [org.bouncycastle.openssl PEMParser PEMEncryptedKeyPair]
           [org.bouncycastle.openssl.jcajce JcePEMDecryptorProviderBuilder]
           [org.bouncycastle.jce.provider BouncyCastleProvider]
           [org.bouncycastle.cert.jcajce JcaX509CertificateConverter]
           [java.security.spec X509EncodedKeySpec PKCS8EncodedKeySpec]
           [java.security KeyFactory KeyPair Security KeyStore]
           [java.io InputStreamReader]))

(defn encrypted-pem-key? [pem-obj]
  (instance? PEMEncryptedKeyPair pem-obj))

(defn load-pem-object [stream]
  (with-open [reader (InputStreamReader. stream)
              parser (PEMParser. reader)]
    (.readObject parser)))

(defn decrypt-pem-key [encrypted password-chars]
  (let [decryptor-builder (JcePEMDecryptorProviderBuilder.)
        decryptor (.build decryptor-builder password-chars)]
    (.decryptKeyPair encrypted decryptor)))

(defn gen-key-pair [pem-key]
  (let [key-factory (KeyFactory/getInstance "RSA")
        public-key-spec (X509EncodedKeySpec. (.. pem-key (getPublicKeyInfo) (getEncoded)))
        private-key-spec (PKCS8EncodedKeySpec. (.. pem-key (getPrivateKeyInfo) (getEncoded)))
        public-key (.generatePublic key-factory public-key-spec)
        private-key (.generatePrivate key-factory private-key-spec)]
    (KeyPair. public-key private-key)))

(defn gen-cert [pem-cert]
  (if (nil? (Security/getProvider "BC"))
    (Security/addProvider (BouncyCastleProvider.)))
  (.. (JcaX509CertificateConverter.) (setProvider "BC") (getCertificate pem-cert)))

(defn gen-key-store [key-pair cert passphrase-chars]
  (let [key-store (KeyStore/getInstance "JKS")
        private-key (.getPrivate key-pair)]
    (.load key-store nil)
    (.setKeyEntry key-store "1" private-key passphrase-chars (into-array [cert]))
    key-store))
