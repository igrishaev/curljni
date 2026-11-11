(ns curl.test-methods
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

      [:dunno "/request"]
      {:status 200
       :body "it was DUNNO method"}


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

(deftest test-post
  (testing "post"
    (let [opts
          {:url (fix/->url "/post")
           :method :post
           :as :string}

          {:keys [status body]}
          (curl/request opts)]

      (is (= 200 status))
      (is (= "it was post" body)))))


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
          (is (= "it was put" body)))))))

;; TODO test head

(deftest test-custom-method
  (let [opts
        {:url (fix/->url "/request")
         :method "DUNNO"
         :body "test"
         :as :string}

        {:keys [status body]}
        (curl/request opts)]

    (is (= 200 status))
    (is (= "it was DUNNO method" body))))
