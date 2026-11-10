(ns curl-test
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
                uri]}
        request]

    (case [request-method uri]
      [:get "/get"]
      {:status 200
       :body "it was get"})))

(fixtures/use-server app)

(defn ->url [path]
  (format "http://127.0.0.1:%s%s" fixtures/PORT path))

(deftest test-get
  (testing "get"
    (let [opts
          {:url (->url "/get")
           :method :get
           :as :string}

          {:keys [status body]}
          (curl/request opts)]

      (is (= 200 status))
      (is (= "it was get" body)))

    (let [opts
          {:url (->url "/get")
           :method "GET"
           :as :string}

          {:keys [status body]}
          (curl/request opts)]

      (is (= 200 status))
      (is (= "it was get" body)))))
