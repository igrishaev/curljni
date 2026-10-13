(def MIN_JAVA_VERSION "13")

(defproject io.github.igrishaev/curlclj "0.1.0-SNAPSHOT"

  :description
  "FIXME: write description"

  :url
  "http://example.com/FIXME"

  :license
  {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
   :url "https://www.eclipse.org/legal/epl-2.0/"}

  :source-paths ["clojure/src"]
  :java-source-paths ["java/main"]
  :resource-paths ["resources"]

  :managed-dependencies
  [[org.clojure/clojure "1.10.0" :scope "provided"]
   [ring/ring-core "1.10.0"]
   [ring/ring-jetty-adapter "1.10.0"]
   [http-kit "2.3.0"]
   [criterium "0.4.6"]
   [clj-http "3.12.0"]
   [org.babashka/http-client "0.4.22"]]

  :dependencies
  [[org.clojure/clojure]]

  :pom-addition
  [:properties
   ["maven.compiler.source" ~MIN_JAVA_VERSION]
   ["maven.compiler.target" ~MIN_JAVA_VERSION]]

  :javac-options ["-Xlint:unchecked"
                  "-Xlint:preview"
                  "--release" ~MIN_JAVA_VERSION]

  :main ^:skip-aot server.core

  :target-path "target/%s"

  :profiles
  {:test
   {:source-paths ["clojure/test"]
    :dependencies
    [[ring/ring-core]
     [ring/ring-jetty-adapter]
     [http-kit]
     [criterium]
     [clj-http]
     [org.babashka/http-client]]}

   :uberjar
   {:aot :all
    :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
