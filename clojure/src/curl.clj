(ns curl
  (:import
   (java.util Map)
   (org.curljni CurlEasy
                WriteCallback
                WriteCallback$IHandler
                CURLFOLLOW
                CURLOPT
                Headers
                FILE
                FN
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

#_
{:url 1
 :scheme ""
 :user 2
 :password 3
 :host ""
 :port 4
 :path 5
 :query 5

 }

(defn error! [template & args]
  (let [message ^String (apply format template args)]
    (throw (new RuntimeException message))))

(defn named? [x]
  (instance? clojure.lang.Named x))

(defn set-method ^CurlEasy [^CurlEasy c method]
  (case method
    ("GET" :get)
    (.setOptionLong c CURLOPT/HTTPGET 1)

    ("POST" :post)
    (-> c
        (.setOptionLong CURLOPT/POST 1)
        (.setOptionLong CURLOPT/POSTFIELDSIZE 0))

    (cond
      (string? method)
      (.setCustomRequest c method)

      (named? method)
      (.setCustomRequest c (name method))

      (nil? method)
      c

      :else
      (error! "wrong method: %s" method))))

(defn set-follow-redirects ^CurlEasy [^CurlEasy c value]
  (case value
    (true :all :yes)
    (.setOptionLong c CURLOPT/FOLLOWLOCATION CURLFOLLOW/ALL)

    (:first :one)
    (.setOptionLong c CURLOPT/FOLLOWLOCATION CURLFOLLOW/FIRSTONLY)

    (:code)
    (.setOptionLong c CURLOPT/FOLLOWLOCATION CURLFOLLOW/OBEYCODE)

    (false :no :off)
    (.setOptionLong c CURLOPT/FOLLOWLOCATION CURLFOLLOW/OFF)

    (nil)
    c

    ;; else
    (error! "wrong follow-redirects: %s" value)))

(defn request
  ([opts]
   (with-open [c (CurlEasy/make)]
     (request c opts)))

  ([^CurlEasy c opts]
   (let [{:keys [

                 ;; keep options?

                 ;; common
                 url
                 method
                 headers
                 user-agent
                 ;; query-params

                 ;; cookies (string, map, file, jar)

                 ;; timeouts
                 timeout-ms
                 server-response-timeout-ms
                 connect-timeout-ms

                 ;; redirects
                 follow-redirects
                 max-redirects

                 ;; input & output
                 ;; body: form-encode multipart file input-stream read-channel bytes string callback
                 ;; as: bytes string file output-stream write-channel callback
                 as
                 body

                 ;; auth and proxy
                 proxy
                 ;; proxy-user
                 ;; proxy-password
                 ;; username
                 ;; password

                 ;; cert options

                 ;; tcp options
                 tcp-fast-open
                 ;; other tcp opts

                 ;; misc
                 max-connects
                 ;; no-body
                 ;; fail-on-error
                 ]}
         opts]

     ;; TODO: param to keep options
     (.resetOptions c)

     (set-method c method)
     (set-follow-redirects c follow-redirects)

     (let [as-bytes?
           (or (nil? as)
               (= as :bytes)
               (= as :byte-array))

           as-string?
           (= as :string)

           accum?
           (or as-bytes? as-string?)

           file?
           (instance? java.io.File as)

           handler?
           (fn? as)]

       ;; open resources
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

         ;; common options
           (cond-> c

             timeout-ms
             (.setOptionLong CURLOPT/TIMEOUT_MS timeout-ms)

             connect-timeout-ms
             (.setOptionLong CURLOPT/CONNECTTIMEOUT_MS connect-timeout-ms)

             server-response-timeout-ms
             (.setOptionLong CURLOPT/SERVER_RESPONSE_TIMEOUT_MS server-response-timeout-ms)

             max-connects
             (.setOptionLong CURLOPT/MAXCONNECTS max-connects)

             max-redirects
             (.setOptionLong CURLOPT/MAXREDIRS max-redirects)

             (some? tcp-fast-open)
             (.setTCPFastOpen tcp-fast-open)

             user-agent
             (.setOptionString CURLOPT/USERAGENT user-agent)

             proxy
             (.setOptionString CURLOPT/PROXY proxy)

             url
             (.setUrl url)

             ;; accum
             ;; (.setWriteParams (.ptr accum) FN/accum_write_callback)

             accum
             (.setOptionLong2 CURLOPT/WRITEDATA
                              (.ptr accum)
                              CURLOPT/WRITEFUNCTION
                              FN/accum_write_callback)

             file
             (.setWriteFile file)

             handler
             (.setWriteCallback handler)

             jheaders
             (.setOptionLong CURLOPT/HTTPHEADER (.ptr jheaders))

             :finally
             (.perform))

           ;; response
           {:status
            (.getResponseCode c)

            :headers
            (.getHeaders c)

            :connect-time-ms
            (.getConnectTimeMs c)

            :content-length
            (.getContentLengthDownload c)

            :redirect-time-ms
            (.getRedirectTime c)

            :retry-after
            (.getRetryAfter c)

            :speed-download
            (.getSpeedDownload c)

            :redirect-count
            (.getRedirectCount c)

            :http-version
            (.getHTTPVersion c)

            :primary-ip
            (.getPrimaryIP c)

            :primary-port
            (.getPrimaryPort c)

            :local-ip
            (.getLocalIP c)

            :local-port
            (.getLocalPort c)

            :effective-url
            (.getEffectiveURL c)

            :proxy-used?
            (.getUsedProxy c)

            :body
            (cond

              as-bytes?
              (.getBytes accum)

              as-string?
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
