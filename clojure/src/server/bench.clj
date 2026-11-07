(ns server.bench
  (:import
   (java.io InputStream
            ByteArrayOutputStream))
  (:use criterium.core)
  (:require
   [curl]
   [org.httpkit.client :as http]
   [babashka.http-client :as bb]
   [clj-http.conn-mgr :as conn]
   [clj-http.client :as client]))

(def URL "http://127.0.0.1:3099")
#_
(def URL "http://google.com")

(defn test-bb []
  (quick-bench
      (let [response
            (bb/get URL
                    {:as :string})])))

(defn test-clj-http []
  (let [cm (conn/make-reusable-conn-manager {})]

    (quick-bench
        (let [response
              (client/get URL
                          #_
                          {:connection-manager cm
                           :cache true})
              {:keys [body]}
              response]

          #_
          (assert (string? body))))

    #_
    (quick-bench
        (let [out
              (new ByteArrayOutputStream)

              response
              (client/get URL
                          {:connection-manager cm
                           :as :stream
                           :cache true})

              {:keys [^InputStream body]}
              response]

          (.transferTo body out)
          (.close out)))))

(defn test-http-kit []
  (quick-bench
      @(http/get URL
                 {:as :byte-array}
                 )

      #_
      (let [out
            (new ByteArrayOutputStream)

            response
            @(http/get URL
                       {:as :stream})

            {:keys [^InputStream body]}
            response]

        (.transferTo body out)
        (.close out))))


(defn test-curl []

  (let [opt {:url URL
             :method :get
             :follow-redirects true
             :headers {"foo" "bar"}
             :as :bytes}]

    #_
    (quick-bench
        (curl/request opt))

    (with-open [c (curl/init)]
      (quick-bench
          (curl/request c opt)))))
