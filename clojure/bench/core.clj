(ns server.core
  (:gen-class)
  (:require
   [clojure.string :as str]
   [ring.adapter.jetty :as jetty]
   [clojure.java.io :as io]))

(def DATA
  (str/join "" (repeat 100000 "ABC")))

(defn app [request]
  {:status  200

   :headers {"Content-Type" "text/plain"}
   :body    DATA

   ;; :headers {"Content-Type" "application/octet-stream"}
   ;; :body    (io/file "resources/sample.pdf")

   })

(def server
  (delay
    (jetty/run-jetty app {:port 3099 :join? false})))

(defn -main [& args]
  (println "Starting Ring server on port 3000...")
  @server)
