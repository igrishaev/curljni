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
      [:get "/too-slow"]
      (do
        (Thread/sleep 1000)
        {:status 200
         :body "finally!"}))))

(fixtures/use-server app)

(defn ->url [path]
  (format "http://127.0.0.1:%s%s" fixtures/PORT path))

(deftest test-server-response-timeout
  (let [opts
        {:url (->url "/too-slow")
         :method :get
         :timeout-ms 900}]

    (testing "timeout triggered"
      (try
        (curl/request opts)
        (is false)
        (catch CURLException e
          (is (-> e
                  (ex-message)
                  (str/includes? "Operation timed out after"))))))

    (testing "this is fine"
      (let [opts
            (merge opts {:timeout-ms 1100})

            {:keys [status]}
            (curl/request opts)]

        (is (= 200 status))))))
