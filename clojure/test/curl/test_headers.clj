(ns curl.test-headers
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
  {:status 200
   :body (json/generate-string
          {:request
           (select-keys request
                        [:request-method
                         :uri
                         :headers])})})

(fix/use-server app)

(deftest test-pass-headers
  (testing "simple"
    (let [opts
          {:url (fix/->url "/")
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
              (get-in ["request" "headers"])
              (dissoc "host" "accept"))]

      (is (= 200 status))
      (is (= {"123" "456"
              "abc" "xyz"}
             headers))))

  (testing "nil"
    (let [opts
          {:url (fix/->url "/")
           :method :get
           :as :string
           :headers nil}

          {:keys [status body]}
          (curl/request opts)

          headers
          (-> body
              json/parse-string
              (get-in ["request" "headers"])
              (dissoc "host" "accept"))]

      (is (= 200 status))
      (is (= {} headers)))))
