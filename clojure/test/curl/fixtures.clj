(ns curl.fixtures
  (:require
   [clojure.test :refer [use-fixtures]]
   [ring.adapter.jetty :as jetty]))

(def PORT 3099)

(def jetty-opts
  {:port PORT :join? false})

(defn use-server [app]
  (use-fixtures :once
    (fn [t]
      (let [server (jetty/run-jetty app jetty-opts)]
        (t)
        (.stop server)))))

(defn ->url [path]
  (format "http://127.0.0.1:%s%s" PORT path))
