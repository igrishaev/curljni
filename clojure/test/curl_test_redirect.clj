(ns curl-test-upload
  (:import
   (org.curljni CURLException))
  (:require
   [any.core :as any]
   [fixtures]
   [cheshire.core :as json]
   [curl :as curl]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :refer [is deftest testing]]))

(defn ->url [path]
  (format "http://127.0.0.1:%s%s" fixtures/PORT path))

(defn app [request]
  (let [{:keys [request-method
                uri
                headers
                body]}
        request

        {:strs [content-length]}
        headers]

    (case [request-method uri]

      [:get "/foo"]
      {:status 302
       :headers {"location" (->url "/bar")}}

      [:get "/bar"]
      {:status 200
       :body "OK"}

      [:put "/"]
      {:status 302
       :headers {"location" (->url "/new-url")}}

      [:put "/new-url"]
      {:status 200
       :body (json/generate-string
              {:method request-method
               :body (slurp body)
               :content-length content-length})}

      {:status 404
       :body (json/generate-string {:message "not found"})})))

(fixtures/use-server app)

(deftest test-get-no-redirects
  (let [opts
        {:url (->url "/foo")
         :method :get
         :as :string}

        {:keys [status body]}
        (curl/request opts)]

    (is (= 302 status))
    (is (= "" body))))

(deftest test-get-redirects-all
  (let [opts
        {:url (->url "/foo")
         :method :get
         :follow-redirects true
         :as :string}

        {:keys [status body effective-url]}
        (curl/request opts)]

    (is (= 200 status))
    (is (= "OK" body))
    (is (= (->url "/bar") effective-url))))

(deftest test-put-file-with-redirect
  (let [read-file
        (io/file "project.clj")

        opts
        {:url (->url "/")
         :method :put
         :as :string
         :follow-redirects true
         :body read-file}

        {:keys [status body]}
        (curl/request opts)

        data
        (json/parse-string body)]

    (is (= 200 status))
    (is (= {"method" "put"
            "body" any/string
            "content-length" "1409"}
           data))))

(deftest test-redirect-with-stream
  (let [in
        (-> "foobarbaz"
            (.getBytes)
            (io/input-stream))

        opts
        {:url (->url "/")
         :method :put
         :follow-redirects true
         :as :string
         :body in}

        {:keys [status body]}
        (curl/request opts)

        data
        (json/parse-string body)]

    ;; <CURLException code: 65, strError: Send failed since rewinding of the data stream failed, strErrorBuffer: seek callback returned error 2, errno: 35, errnoMessage: Resource temporarily unavailable>

    (is (= 200 status))
    (is (= {"method" "patch"
            "body" "foobarbaz"
            "content-length" nil}
           data))))
