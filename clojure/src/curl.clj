(ns curl
  (:import
   (java.util Map)
   (org.curljni CurlEasy
                WriteCallback
                WriteCallback$IHandler
                CURLFOLLOW
                Headers
                Dummy
                FILE
                Accumulator)))

(set! *warn-on-reflection* true)

(defn init ^CurlEasy []
  (CurlEasy/make))

(defmacro with-resources [bindings & body]
  (if (= (count bindings) 0)
    `(do ~@body)
    `(let ~(subvec bindings 0 2)
       (try
         (with-resources ~(subvec bindings 2)
           ~@body)
         (finally
           (when (some? ~(bindings 0))
             (.close ~(bindings 0))))))))

(defn request
  ([opts]
   (with-open [c (CurlEasy/make)]
     (request c opts)))

  ([^CurlEasy c opts]
   (let [{:keys [url
                 method
                 headers
                 as
                 follow-redirects
                 body
                 ]}
         opts]

     (.resetOptions c)

     (case follow-redirects

       (true :all :yes)
       (.setFollowLocation c CURLFOLLOW/ALL)

       (:first :one)
       (.setFollowLocation c CURLFOLLOW/FIRSTONLY)

       (:code)
       (.setFollowLocation c CURLFOLLOW/OBEYCODE)

       (false :no :off)
       (.setFollowLocation c CURLFOLLOW/OFF))

     (case method
       (:get "GET")
       (.setHTTPGet c true)

       (:post "POST")
       (.setHTTPPost c true)

       ;; else
       (when method
         (.setCustomRequest c method)))

     (let [accum?
           (or (= as :bytes)
               (= as :string))

           file?
           (instance? java.io.File as)

           handler?
           (fn? as)]

       (with-resources
           [accum
            (when accum?
              (Accumulator/create))

            file
            (when file?
              (FILE/open (.getAbsolutePath ^java.io.File as) "wb"))

            jheaders
            (when (seq headers)
              (Headers/create ^Map headers))

            handler
            (when handler?
              (WriteCallback/create
               (reify WriteCallback$IHandler
                 (handle [this bb]
                   (as bb)))))]

           (cond-> c

             url
             (.setUrl url)

             accum
             (.setAccumulator accum)

             file
             (.setWriteFile file)

             handler
             (.setWriteCallback handler)

             jheaders
             (.setHeaders jheaders)

             :finally
             (.perform))

           {:status (.getResponseCode c)
            :headers (.getHeaders c)
            :body
            (cond

              (= as :bytes)
              (.getBytes accum)

              (= as :string)
              (.getString accum)

              file
              as

              )})))))

(comment

  (request {:url "https://habr.com"
            :method "GET"
            :follow-redirects true
            :as :string
            :headers {"foo" "bar"}})

  (request {:url "https://habr.com"
            :method "GET"
            :follow-redirects true
            :as (fn [bb]
                  (println bb))
            :headers {"foo" "bar"}})

  (with-open [c (init)]
    (time (request c {:url "https://habr.com"
                      :method "GET"
                      :follow-redirects true
                      :as (clojure.java.io/file "output.html")
                      :headers {"foo" "bar"}})))

)
