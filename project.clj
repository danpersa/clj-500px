(defproject clj-500px "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-oauth "1.5.5"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot clj-500px.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
