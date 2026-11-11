(ns curl.test-simple
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
                uri
                headers]}
        request]

    (case [request-method uri]

      [:get "/"]
      {:status 200
       :body "test"
       :headers {"content-type" "text/plain"
                 "content-length" "4"}}

      [:get "/too-slow"]
      (do
        (Thread/sleep 1000)
        {:status 200
         :body "finally!"})

      [:get "/check-headers"]
      {:status 200
       :body (json/generate-string {:headers headers})}

      [:get "/get"]
      {:status 200
       :body "it was get"}

      [:post "/post"]
      {:status 200
       :body "it was post"}

      [:put "/put"]
      {:status 200
       :body "it was put"}

      ;; else
      {:status 404
       :body "no such method"
       :headers {"content-type" "text/plain"}})))

(fix/use-server app)

(deftest test-ok
  (let [opts
        {:url (fix/->url "/")
         :method :get}

        {:keys [status ^bytes body]}
        (curl/request opts)]

    (is (= 200 status))
    (is (bytes? body))
    (is (= "test" (new String body)))))

(deftest test-as-string
  (let [opts
        {:url "http://127.0.0.1:3099/"
         :method :get
         :as :string}

        {:keys [status body]}
        (curl/request opts)]

    (is (= 200 status))
    (is (= "test" body))))

(deftest test-user-agent
  (testing "a string value"
    (let [opts
          {:url (fix/->url "/check-headers")
           :method :get
           :as :string
           :user-agent "a teapot"}

          {:keys [status body]}
          (curl/request opts)

          headers
          (-> body
              json/parse-string
              (get "headers")
              (dissoc "host" "accept"))]

      (is (= 200 status))
      (is (= {"user-agent" "a teapot"} headers))))

  (testing "null"
    (let [opts
          {:url (fix/->url "/check-headers")
           :method :get
           :as :string
           :user-agent nil}

          {:keys [status body]}
          (curl/request opts)

          headers
          (-> body
              json/parse-string
              (get "headers")
              (dissoc "host" "accept"))]

      (is (= 200 status))
      (is (= {} headers)))))
