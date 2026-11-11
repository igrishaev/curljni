(ns curl.test-upload
  (:import
   (java.io InputStream)
   (org.curljni CURLException))
  (:require
   [any.core :as any]
   [curl.fixtures :as fix]
   [cheshire.core :as json]
   [curl.core :as curl]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :refer [is deftest testing]]))

(defn app [request]
  (let [{:keys [request-method
                headers
                body]}
        request

        {:strs [content-length]}
        headers]

    {:status 200
     :body (json/generate-string
            {:method request-method
             :body (slurp body)
             :content-length content-length})}))

(fix/use-server app)

(deftest test-post-bytes
  (let [opts
        {:url (fix/->url "/")
         :method :post
         :as :string
         :body (.getBytes "foobarbaz")}

        {:keys [status body]}
        (curl/request opts)

        data
        (json/parse-string body)]

    (is (= 200 status))
    (is (= {"method" "post"
            "body" "foobarbaz"
            "content-length" "9"}
           data))))

(deftest test-post-string
  (let [opts
        {:url (fix/->url "/")
         :method "POST"
         :as :string
         :body "hello"}

        {:keys [status body]}
        (curl/request opts)

        data
        (json/parse-string body)]

    (is (= 200 status))
    (is (= {"method" "post"
            "body" "hello"
            "content-length" "5"}
           data))))

(deftest test-get-with-bytes
  (let [opts
        {:url (fix/->url "/")
         :method :get
         :as :string
         :body (.getBytes "foobarbaz")}

        {:keys [status body]}
        (curl/request opts)

        data
        (json/parse-string body)]

    (is (= 200 status))
    (testing "method switches to POST when payload is set"
      (is (= {"method" "post"
              "body" "foobarbaz"
              "content-length" "9"}
             data)))))

(deftest test-put-with-bytes
  (let [opts
        {:url (fix/->url "/")
         :method :put
         :as :string
         :body (.getBytes "foobarbaz")}

        {:keys [status body]}
        (curl/request opts)

        data
        (json/parse-string body)]

    (is (= 200 status))
    (is (= {"method" "put"
            "body" "foobarbaz"
            "content-length" "9"}
           data))))

(deftest test-delete-with-bytes
  (let [opts
        {:url (fix/->url "/")
         :method "DELETE"
         :as :string
         :body (.getBytes "foobarbaz")}

        {:keys [status body]}
        (curl/request opts)

        data
        (json/parse-string body)]

    (is (= 200 status))
    (is (= {"method" "delete"
            "body" "foobarbaz"
            "content-length" "9"}
           data))))

(deftest test-delete-empty
  (let [opts
        {:url (fix/->url "/")
         :method "DELETE"
         :as :string}

        {:keys [status body]}
        (curl/request opts)

        data
        (json/parse-string body)]

    (is (= 200 status))
    (is (= {"method" "delete"
            "body" ""
            "content-length" nil}
           data))))

(deftest test-custom-method
  (let [opts
        {:url (fix/->url "/")
         :method "DUNNO"
         :body "test"
         :as :string}

        {:keys [status body]}
        (curl/request opts)

        data
        (json/parse-string body)]

    (is (= 200 status))
    (is (= {"method" "dunno"
            "body" "test"
            "content-length" "4"}
           data))))

(deftest test-put-with-file
  (let [read-file
        (io/file "project.clj")

        opts
        {:url (fix/->url "/")
         :method :put
         :as :string
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

(deftest test-put-missing-file
  (let [read-file
        (io/file "dunno-missing.clj")

        opts
        {:url (fix/->url "/")
         :method :put
         :as :string
         :body read-file}]

    (try
      (curl/request opts)
      (is false)
      (catch RuntimeException e
        (is (-> e
                (ex-message)
                (str/includes? "fopen failed")))))))

(deftest test-patch-with-stream
  (let [in
        (-> "foobarbaz"
            (.getBytes)
            (io/input-stream))

        opts
        {:url (fix/->url "/")
         :method :patch
         :as :string
         :body in}

        {:keys [status body]}
        (curl/request opts)

        data
        (json/parse-string body)]

    (is (= 200 status))
    (is (= {"method" "patch"
            "body" "foobarbaz"
            "content-length" nil}
           data))))

;; try channel

(deftest test-stream-with-exception
  (let [f
        (fn [] (throw (ex-info "boom" {})))

        in
        (proxy [InputStream] []
          (read
            ([] (f))
            ([a] (f))
            ([a b] (f))
            ([a b c] (f))
            ([a b c d] (f))))

        opts
        {:url (fix/->url "/")
         :method :patch
         :as :string
         :body in}]

    (try
      (curl/request opts)
      (is false)
      (catch Exception e
        (is (-> e
                (ex-message)
                (= "boom")))))))
