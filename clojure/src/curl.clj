(ns curl
  (:import
   (java.util Map)
   (org.curljni CurlEasy
                IResource
                Headers
                Dummy
                FILE
                Accumulator)))

#_
(defn ->request ^Request [opts]

  (let [{:keys [url
                method
                follow-location
                resource-headers
                resource-write-file
                resource-accum]}
        opts]

    (cond-> (Request/builder)

      ;; TODO URL URI
      (string? url)
      (.url ^String url)

      method
      (.method method)

      follow-location
      (.followLocation follow-location)

      resource-write-file
      (.writeFile resource-write-file)

      resource-headers
      (.headers resource-headers)

      resource-accum
      (.accum resource-accum)

      :finally
      (.build))))

(defn init ^CurlEasy []
  (CurlEasy/make))

#_
(defn perform [^CurlEasy curl opts]
  (.perform curl (->request opts)))

(defn open-headers ^IResource [headers]
  (if headers
    (Headers/create ^Map headers)
    Dummy/INSTANCE))

(defn open-write-file ^IResource [write-file]
  (if write-file
    (FILE/open ^String write-file "wb")
    Dummy/INSTANCE))

(defn open-accum ^IResource [accumulate?]
  (if accumulate?
    (Accumulator/create 4096)
    Dummy/INSTANCE))

(defn ->response [^CurlEasy curl accumulate? ^Accumulator acc]
  {:status (.getResponseCode curl)
   :headers (.getHeaders curl)
   :body (when accumulate?
           (.getBytes acc))})

(defn perform ^CurlEasy [^CurlEasy curl opts]
  (let [{:keys [url
                method
                headers
                follow-location
                write-file
                accumulate?]}
        opts]
    (with-open [h (open-headers headers)
                f (open-write-file write-file)
                a (open-accum accumulate?)]
      (cond-> (.resetOptions curl)

        url
        (.setUrl url)

        method
        (.setMethod method)

        follow-location
        (.setFollowLocation follow-location)

        :true
        (.setAccumulator a)

        :true
        (.setHeaders h)

        :then
        (.perform)

        :finally
        (->response accumulate? a)))))

(comment

  (with-open [c (init)]
    (perform c {:url "https://habr.com" ;; "http://127.0.0.1:3000"
                :method 1
                :follow-location 3
                :headers {"foo" "bar"}
                :accumulate? true})))
