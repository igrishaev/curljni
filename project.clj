(def MIN_JAVA_VERSION "13")

(defproject io.github.igrishaev/curlclj "0.1.0-SNAPSHOT"

  :description
  "FIXME: write description"

  :url
  "http://example.com/FIXME"

  :license
  {:name "The Unlicense"
   :url "https://choosealicense.com/licenses/unlicense/"}

  :source-paths ["clojure/src"]
  :java-source-paths ["java/main"]

  :managed-dependencies
  [[org.clojure/clojure "1.10.0" :scope "provided"]
   [ring/ring-core "1.10.0"]
   [ring/ring-jetty-adapter "1.10.0"]
   [http-kit "2.3.0"]
   [com.github.igrishaev/any "0.1.1"]
   [criterium "0.4.6"]
   [clj-http "3.12.0"]
   [cheshire "5.10.0"]
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

  :profiles
  {:bench
   {:source-paths ["clojure/bench"]
    :dependencies
    [[ring/ring-core]
     [ring/ring-jetty-adapter]
     [cheshire]
     [http-kit]
     [criterium]
     [clj-http]
     [org.babashka/http-client]]}

   :test
   {
    :jvm-opts ["--enable-native-access=ALL-UNNAMED"
               "-Dorg.eclipse.jetty.LEVEL=OFF"
               "-Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog"]

    :test-paths ["clojure/test"]
    :resource-paths ["clojure/test/resources"]
    :dependencies
    [[ring/ring-core]
     [ring/ring-jetty-adapter]
     [com.github.igrishaev/any]
     [cheshire]]}

   :uberjar
   {:aot :all
    :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
