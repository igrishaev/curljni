(ns curl.test-timeouts
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
      [:get "/too-slow"]
      (do
        (Thread/sleep 1000)
        {:status 200
         :body "finally!"}))))

(fix/use-server app)

(deftest test-server-response-timeout
  (let [opts
        {:url (fix/->url "/too-slow")
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
