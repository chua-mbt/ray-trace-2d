(defproject ray-trace-2d "0.1.0"
  :description "2D ray trace problem and solution generator."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [net.mikera/vectorz-clj "0.14.0"]
                 [org.clojure/tools.cli "0.2.4"]
                 [seesaw "1.4.3"]]
  :main ray-trace-2d.core
  :aot [ray-trace-2d.core])
