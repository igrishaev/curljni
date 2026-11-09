(ns curl-test
  (:import
   (org.curljni CURLException))
  (:require
   [cheshire.core :as json]
   [curl :as curl]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :refer [is deftest testing use-fixtures]]
   [ring.adapter.jetty :as jetty]))

(def PORT 3099)

(def jetty-opts
  {:port PORT :join? false})

(defn app [request]
  (let [{:keys [request-method
                uri
                headers]}
        request
        ]

    {:status 200
     :body (json/generate-string
            {:request
             (select-keys [:request-method
                           :uri
                           :headers]
                          request)})}

    #_
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
       :headers {"content-type" "text/plain"}}

      )))

(use-fixtures :once
  (fn [t]
    (let [server (jetty/run-jetty #'app jetty-opts)]
      (t)
      (.stop server))))

(defn ->url [path]
  (format "http://127.0.0.1:%s%s" PORT path))

(deftest test-ok
  (let [opts
        {:url (->url "/")
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

(deftest test-pass-headers
  (testing "simple"
    (let [opts
          {:url (->url "/check-headers")
           :method :get
           :as :string
           :headers {"foo" nil
                     nil "kek"
                     "abc" "xyz"
                     "aaa" ""
                     "" "bbb"
                     "123" "456"}}

          {:keys [status body]}
          (curl/request opts)

          headers
          (-> body
              json/parse-string
              (get "headers")
              (dissoc "host" "accept"))]

      (is (= 200 status))
      (is (= {"abc" "xyz"
              "123" "456"}
             headers))))

  (testing "nil"
    (let [opts
          {:url (->url "/check-headers")
           :method :get
           :as :string
           :headers nil}

          {:keys [status body]}
          (curl/request opts)

          headers
          (-> body
              json/parse-string
              (get "headers")
              (dissoc "host" "accept"))]

      (is (= 200 status))
      (is (= {} headers)))))


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


(deftest test-post
  (testing "post"
    (let [opts
          {:url (->url "/post")
           :method :post
           :as :string}

          {:keys [status body]}
          (curl/request opts)]

      (is (= 200 status))
      (is (= "" body)))))


(deftest test-cumstom-methods


  (testing "put"
    (doseq [method [:put :pUt :PUT "pUt" "Put" "PUT" 'put 'PUT]]
      (testing (str method)
        (let [opts
              {:url (->url "/put")
               :as :string
               :method method}

              {:keys [status body]}
              (curl/request opts)]

          (is (= 200 status))
          (is (= "it was put" body))))))

  ;; test head

  )

(deftest test-user-agent
  (testing "a string value"
    (let [opts
          {:url (->url "/check-headers")
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
          {:url (->url "/check-headers")
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
