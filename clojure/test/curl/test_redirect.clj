(ns curl.test-redirect
  (:import
   (org.curljni CURLException)
   (java.nio.channels Channels))
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
                uri
                headers
                body]}
        request

        {:strs [content-length]}
        headers]

    (case [request-method uri]

      [:get "/goto-a"]
      {:status 302
       :headers {"location" (fix/->url "/goto-b")}}

      [:get "/goto-b"]
      {:status 302
       :headers {"location" (fix/->url "/goto-a")}}

      [:get "/foo"]
      {:status 302
       :headers {"location" (fix/->url "/bar")}}

      [:get "/bar"]
      {:status 200
       :body "OK"}

      [:put "/"]
      {:status 302
       :headers {"location" (fix/->url "/new-url")}}

      [:put "/new-url"]
      {:status 200
       :body (json/generate-string
              {:method request-method
               :body (slurp body)
               :content-length content-length})}

      {:status 404
       :body (json/generate-string {:message "not found"})})))

(fix/use-server app)

(deftest test-get-no-redirects
  (let [opts
        {:url (fix/->url "/foo")
         :method :get
         :as :string}

        {:keys [status body]}
        (curl/request opts)]

    (is (= 302 status))
    (is (= "" body))))

(deftest test-get-redirects-all
  (let [opts
        {:url (fix/->url "/foo")
         :method :get
         :follow-redirects true
         :as :string}

        {:keys [status body effective-url]}
        (curl/request opts)]

    (is (= 200 status))
    (is (= "OK" body))
    (is (= (fix/->url "/bar") effective-url))))

(deftest test-put-file-with-redirect
  (let [read-file
        (-> "textfile.txt" io/resource io/file)

        opts
        {:url (fix/->url "/")
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
            "content-length" "12"}
           data))))

(deftest test-get-redirect-loop
  (let [opts
        {:url (fix/->url "/goto-a")
         :method :get
         :as :string
         :follow-redirects true}]

    (try
      (curl/request opts)
      (is false)
      (catch CURLException e
        (is (-> e
                (ex-message)
                (str/includes? "Maximum (30) redirects followed"))))))

  (let [opts
        {:url (fix/->url "/goto-a")
         :method :get
         :as :string
         :max-redirects 12
         :follow-redirects true}]

    (try
      (curl/request opts)
      (is false)
      (catch CURLException e
        (is (-> e
                (ex-message)
                (str/includes? "Maximum (12) redirects followed")))))))


(defn gen-string ^String [^String sample n]
  (str/join \space (repeat n sample)))


(deftest test-redirect-with-stream
  (let [content
        (gen-string "abc" 100000)

        in
        (-> content
            (.getBytes)
            (io/input-stream)
            (Channels/newChannel))

        opts
        {:url (fix/->url "/")
         :method :put
         :follow-redirects true
         :as :string
         :body in}

        [response e]
        (try ;; 50/50
          [(curl/request opts) nil]
          (catch CURLException e
            [nil e]))

        {:keys [status body]}
        response

        data
        (when body
          (json/parse-string body))

        {:strs [content-length]}
        data]

    (if e
      (is (-> e
              (ex-message)
              (str/includes? "rewinding of the data stream failed")))
      (do
        (is (= 200 status))
        (is (= nil content-length))))))
