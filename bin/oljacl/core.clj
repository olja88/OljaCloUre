(ns oljacl.core
(:use [compojure.core :only (defroutes)]
        [ring.adapter.jetty :as ring])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [oljacl.controller.tests :as controler]
            [oljacl.view.layout :as layout]
            [oljacl.model.migration :as schema])
  (:gen-class))

(defroutes routes
  controler/routes
  (route/resources "/")
  (route/not-found (layout/err404-template)))

(def application (handler/site routes))

(defn start [port]
  (run-jetty application {:port port
                          :join? false}))

(defn -main []
  (schema/migrate)
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (start port)))