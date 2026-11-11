(ns curl.test
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

(fix/use-server app)

(deftest test-pass-headers
  (testing "simple"
    (let [opts
          {:url (fix/->url "/check-headers")
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
          {:url (fix/->url "/check-headers")
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


(deftest test-post
  (testing "post"
    (let [opts
          {:url (fix/->url "/post")
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
              {:url (fix/->url "/put")
               :as :string
               :method method}

              {:keys [status body]}
              (curl/request opts)]

          (is (= 200 status))
          (is (= "it was put" body))))))

  ;; test head

  )
