(defproject OljaCloUre "0.1.0"
  :description "Clojure app powered by Olja Latinović"
  :url "https://github.com/olja88/OljaCloUre"
  :min-lein-version "2.0.0"
  :license {:name "The MIT License"
            :url "https://raw.githubusercontent.com/olja88/OljaCloUre/master/LICENSE"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/java.jdbc "0.3.3"]
                 [postgresql "9.1-901.jdbc4"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [com.cemerick/friend "0.2.0"]
                 [bultitude "0.2.6"]
                 [org.webjars/foundation "5.1.1"]]
  
  :aliases  {"sanity-check" ["do" "clean," "compile" ":all," "clean"]}
  
  :main oljacl.core
  
  :aot [oljacl.core]

  :ring {:handler oljacl.core/erp
         :init oljacl.core/init})