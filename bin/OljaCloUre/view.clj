(ns OljaCloUre.view
  (:require [compojure.core :refer [defroutes GET]]
            [ring.adapter.jetty :as ring]))

(defroutes routes
  (GET "/" [] "<h1>Dummy ruta u root-u</h1>"))

(defn -main []
  (ring/run-jetty #'routes {:port 8080 :join? false}))