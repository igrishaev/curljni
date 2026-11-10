(ns curl
  (:import
   (java.io File
            InputStream)
   (java.nio.channels ReadableByteChannel)
   (java.util Map)
   (org.curljni CurlEasy
                Native
                ReadStream
                WriteCallback
                WriteCallback$IHandler
                CURLFOLLOW
                CURLOPT
                Headers
                IPeer
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
      (.setOptionString c CURLOPT/CUSTOMREQUEST method)

      (named? method)
      (.setOptionString c CURLOPT/CUSTOMREQUEST (name method))

      (nil? method)
      c

      :else
      (error! "wrong method value: %s" method))))

(defn set-follow-redirects ^CurlEasy [^CurlEasy c value]
  (case value
    (true :all :yes)
    (.setOptionLong c CURLOPT/FOLLOWLOCATION CURLFOLLOW/ALL)

    (:first :one)
    (.setOptionLong c CURLOPT/FOLLOWLOCATION CURLFOLLOW/FIRSTONLY)

    (:code :obey)
    (.setOptionLong c CURLOPT/FOLLOWLOCATION CURLFOLLOW/OBEYCODE)

    (false :no :off)
    (.setOptionLong c CURLOPT/FOLLOWLOCATION CURLFOLLOW/OFF)

    (nil)
    c

    ;; else
    (error! "wrong follow-redirects value: %s" value)))

(defn set-write-file ^CurlEasy [^CurlEasy c ^IPeer wf]
  (.setWriteParams c (.ptr wf) FN/fwrite))

(defn set-read-stream ^CurlEasy [^CurlEasy c ^IPeer read-stream]
  (-> c
      (.setOptionLong CURLOPT/UPLOAD 1)
      (.setReadParams (.ptr read-stream) FN/ReadStream_handler)
      (.setSeekParams Native/NULL FN/ReadStream_seek)))

(defn set-read-file ^CurlEasy [^CurlEasy c ^FILE f]
  ;; TODO
  ;; windows read_callback_file
  ;; windows read_seek_file
  (.setOptionLong c CURLOPT/UPLOAD 1)
  (.setOptionLong c CURLOPT/INFILESIZE (.fileSize f))
  (.setReadParams c (.ptr f) FN/fread))


;; todo test
;; query params
;; form mime data
;; form multipart
;; max redirects
;; read callback
;; write file
;; write stream
;; write callback
;; proxy
;; ssl cert
;; fail on error

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
                 oauth2-bearer
                 ;; query-params

                 ;; cookies (string, map, file, jar)
                 cookie

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
                 ssl-cert
                 ssl-key
                 ssl-key-password

                 ;; tcp
                 tcp-fast-open?
                 tcp-keep-alive?
                 tcp-keep-cnt
                 tcp-keep-idle
                 tcp-keep-intvl
                 tcp-no-delay?

                 ;; misc
                 verbose?
                 max-connects
                 fail-on-error?
                 no-body?
                 buffer-size
                 upload?



                 ;;
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

           read-file?
           (instance? File body)

           handler?
           (fn? as)]

       ;; open resources
       (with-resources
         [accum
          (when accum?
            (Accumulator/create))

          write-file
          (cond
            (instance? File as)
            (FILE/open ^File as "wb"))

          read-file
          (when read-file?
            (FILE/open ^File body "rb"))

          read-stream
          (cond
            (instance? InputStream body)
            (ReadStream/open ^InputStream body)
            (instance? ReadableByteChannel body)
            (ReadStream/open ^ReadableByteChannel body))

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

           ;; TCP
           (some? tcp-fast-open?)
           (.setOptionBool CURLOPT/TCP_FASTOPEN tcp-fast-open?)

           (some? tcp-keep-alive?)
           (.setOptionBool CURLOPT/TCP_KEEPALIVE tcp-keep-alive?)

           tcp-keep-cnt
           (.setOptionLong CURLOPT/TCP_KEEPCNT tcp-keep-cnt)

           tcp-keep-idle
           (.setOptionLong CURLOPT/TCP_KEEPIDLE tcp-keep-idle)

           tcp-keep-intvl
           (.setOptionLong CURLOPT/TCP_KEEPINTVL tcp-keep-intvl)

           (some? tcp-no-delay?)
           (.setOptionBool CURLOPT/TCP_NODELAY tcp-no-delay?)

           (some? fail-on-error?)
           (.setOptionBool CURLOPT/FAILONERROR fail-on-error?)

           (some? no-body?)
           (.setOptionBool CURLOPT/NOBODY no-body?)

           oauth2-bearer
           (.setOptionString CURLOPT/XOAUTH2_BEARER oauth2-bearer)

           user-agent
           (.setOptionString CURLOPT/USERAGENT user-agent)

           proxy
           (.setOptionString CURLOPT/PROXY proxy)

           url
           (.setUrl url)

           (bytes? body)
           (.setPostData ^bytes body)

           (string? body)
           (.setPostData ^String body)

           accum
           (.setOptionLong2 CURLOPT/WRITEDATA
                            (.ptr accum)
                            CURLOPT/WRITEFUNCTION
                            FN/accum_write_callback)

           read-stream
           (set-read-stream read-stream)

           read-file
           (set-read-file read-file)

           (some? verbose?)
           (.setOptionBool CURLOPT/VERBOSE verbose?)

           write-file
           (set-write-file write-file)

           ssl-cert
           (.setOptionString CURLOPT/SSLCERT ssl-cert)

           ssl-key
           (.setOptionString CURLOPT/SSLKEY ssl-key)

           ssl-key-password
           (.setOptionString CURLOPT/KEYPASSWD ssl-key-password)

           handler
           (.setWriteCallback handler)

           jheaders
           (.setOptionLong CURLOPT/HTTPHEADER (.ptr jheaders))

           ;; cookie
           cookie
           (.setOptionString CURLOPT/COOKIE cookie)

           ;; misc
           buffer-size
           (.setOptionLong CURLOPT/BUFFERSIZE buffer-size)

           ;; (some? upload?)
           ;; (.setOptionBool CURLOPT/UPLOAD upload?)

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

            write-file
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
