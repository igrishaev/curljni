(ns curl
  (:import
   (java.util Map)
   (org.curljni CurlEasy
                WriteCallback
                WriteCallback$IHandler
                CURLFOLLOW
                Headers
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
                 ;; query-params
                 ;; body form-encode multipart file input-stream read-channel bytes string callback
                 ;; as bytes string file output-stream write-channel callback

                 method
                 timeout-ms
                 connect-timeout-ms
                 headers
                 max-redirects
                 as
                 follow-redirects
                 body
                 user-agent
                 proxy
                 ;; proxy-user
                 ;; proxy-password
                 ;; username
                 ;; password
                 tcp-fast-open
                 ;; other tcp opts
                 ;; no-body
                 ;; fail-on-error
                 ]}
         opts]

     ;; TODO: param to keep options
     (.resetOptions c)

     ;; method
     (case method
       ("GET" :get :GET "get")
       (.setHTTPGet c true)

       ("POST" :post :POST "post")
       (.setHTTPPost c true)

       ;; else
       (when method
         (.setCustomRequest c method)))

     ;; redirects
     (case follow-redirects

       (true :all :yes)
       (.setFollowLocation c CURLFOLLOW/ALL)

       (:first :one)
       (.setFollowLocation c CURLFOLLOW/FIRSTONLY)

       (:code)
       (.setFollowLocation c CURLFOLLOW/OBEYCODE)

       (false :no :off)
       (.setFollowLocation c CURLFOLLOW/OFF))

     (let [accum?
           (or (= as :bytes)
               (= as :string))

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
           (.setTimeoutMs ^long timeout-ms)

           connect-timeout-ms
           (.setConnectTimeoutMs ^long connect-timeout-ms)

           max-redirects
           (.setMaxRedirects max-redirects)

           (some? tcp-fast-open)
           (.setTCPFastOpen tcp-fast-open)

           user-agent
           (.setUserAgent user-agent)

           proxy
           (.setProxy proxy)

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
