(ns curl-test-upload
  (:import
   (org.curljni CURLException))
  (:require
   [fixtures]
   [cheshire.core :as json]
   [curl :as curl]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :refer [is deftest testing]]))

(defn app [request]
  (let [{:keys [request-method
                uri
                headers
                body]}
        request
        ]

    {:status 200
     :body (json/generate-string {:body (slurp body)})
     }))

(fixtures/use-server app)

(defn ->url [path]
  (format "http://127.0.0.1:%s%s" fixtures/PORT path))

(deftest test-body-string
  (let [opts
        {:url (->url "/")
         :method "POST"
         :as :string
         :verbose? true
         :body (.getBytes "hello")
         ;; :headers {"content-type" "text/plain"}

         }

        {:keys [status body]}
        (curl/request opts)]

    (is (= 200 status))
    (is (= 1 body))
    ))
