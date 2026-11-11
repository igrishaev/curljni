(ns bench
  (:import
   (java.io InputStream
            ByteArrayOutputStream))
  (:use criterium.core)
  (:require
   [clojure.java.io :as io]
   [clojure.string :as str]
   [curl.core :as curl]
   [org.httpkit.client :as http]
   [babashka.http-client :as bb]
   [ring.adapter.jetty :as jetty]
   [clj-http.conn-mgr :as conn]
   [clj-http.client :as client]))

(def PORT 3091)

(def URL (format "http://127.0.0.1:%d" PORT))

(def ^String DATA
  (str/join "" (repeat 100000 "ABC")))

(defn app [request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body DATA})

(defonce server
  (delay
    (jetty/run-jetty app {:port PORT :join? false})))

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
      @(http/post URL
                  {:body DATA
                   :as :byte-array}
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

  #_
  (let [opt {:url URL
             :method :post
             :body DATA
             :follow-redirects true
             :headers {"foo" "bar"}
             :as :bytes}]
    (with-open [c (curl/init)]
      (time
       (dotimes [x 1]
         (curl/request c opt)))))


  (let [opt {:url URL
             :method :post
             :body DATA
             :follow-redirects true
             :headers {"foo" "bar"}
             :as :bytes}]

    #_
    (quick-bench
        (curl/request opt))

    (with-open [c (curl/init)]
      (quick-bench
          (curl/request c opt)))))
