(ns bench
  (:import
   (java.io InputStream
            ByteArrayOutputStream))
  (:use criterium.core)
  (:require
   [org.httpkit.server :as server]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [curl.core :as curl]
   [org.httpkit.client :as http]
   [babashka.http-client :as bb]
   [ring.adapter.jetty :as jetty]
   [clj-http.conn-mgr :as conn]
   [clj-http.client :as client]))

(def PORT 3091)
(def PORT2 3092)

(def URL (format "http://127.0.0.1:%d" PORT))
(def URL2 (format "http://127.0.0.1:%d" PORT2))

(def ^String DATA
  (str/join "" (repeat 99999999 "A")))

(defn app [request]
  {:status 200
   :headers {"Content-Type" "text/plain"
             "Content-Length" (str (count DATA))}
   :body DATA})

(defonce server
  (delay
    (server/run-server #'app {:port PORT :max-body (count DATA)})
    #_
    (jetty/run-jetty #'app {:port POR2 :join? false})))

(defonce server2
  (delay
    (jetty/run-jetty #'app {:port PORT2 :join? false})))

(defn test-bb []
  (quick-bench
      (let [response
            (bb/get URL
                    {:body DATA
                     :as :bytes
                     :headers {;; "Content-Length" (str (count DATA))
                                     "Content-Type" "text/plain"
                               ;; "Connection" "Keep-Alive"
                               }})])))

(defn test-clj-http []
  (let [cm (conn/make-reusable-conn-manager {})]
    (quick-bench
        (let [response
              (client/get URL
                          {:body DATA
                           :as :byte-array
                           :headers {;; "Content-Length" (str (count DATA))
                                     "Content-Type" "text/plain"
                                     "Connection" "Keep-Alive"}
                           :connection-manager cm
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
                   :as :byte-array
                   :headers {"Content-Length" (str (count DATA))
                             "Content-Type" "text/plain"
                             "Connection" "Keep-Alive"}}
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
             :body (.getBytes DATA)
             :follow-redirects true
             ;; :as nil
             :headers {"foo" "bar"
                       "Expect" nil
                       "Content-Length" (str (count DATA))
                       "Content-Type" "text/plain"
                       "Connection" "Keep-Alive"
                       ;; "Connection" "close"
                       }

             ;; :verbose? true
             ;; :tcp-keep-alive? true
             ;; :tcp-fast-open? true
             ;; :tcp-no-delay? true
             ;; :upload? true

             :as :bytes
             ;; :as (io/file "foo.out")
             ;; :buffer-size 10000000
             ;; :no-body? true
             }]

    #_
    (quick-bench
        (curl/request opt))

    (with-open [c (curl/init)]
      (quick-bench
          (curl/request c opt)))))
