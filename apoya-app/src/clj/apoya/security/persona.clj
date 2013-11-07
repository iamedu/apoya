(ns apoya.security.persona
  (:require [clojure.data.json :as j]
            [clj-http.client :as http]))

(defn verify-assertion
  "Return the raw verification response as a map."
  [assertion audience]
  (if-let [http-response (http/post "https://verifier.login.persona.org/verify"
                                    {:form-params {:assertion assertion
                                                   :audience audience}})]
    (let [verification-response (j/read-json (:body http-response))]
      (if (= 200 (:status http-response))
        verification-response))
    {:status "HTTP POST returned nil/false"}))

(defn valid?
  "Return true if the verification response confirms this user's identity."
  [verification-response]
  (= "okay" (:status verification-response)))
