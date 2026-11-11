(ns curl.test-test
  (:import
   (org.curljni CURLException))
  (:require
   [curl.fixtures :as fix]
   [cheshire.core :as json]
   [curl.core :as curl]
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

(fix/use-server app)

(deftest test-get
  (testing "get"
    (let [opts
          {:url (fix/->url "/get")
           :method :get
           :as :string}

          {:keys [status body]}
          (curl/request opts)]

      (is (= 200 status))
      (is (= "it was get" body)))

    (let [opts
          {:url (fix/->url "/get")
           :method "GET"
           :as :string}

          {:keys [status body]}
          (curl/request opts)]

      (is (= 200 status))
      (is (= "it was get" body)))))
