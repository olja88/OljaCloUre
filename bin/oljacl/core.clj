(ns oljacl.core
(:use [compojure.core :only (defroutes)]
        [ring.adapter.jetty :as ring])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [shouter.controller.tests :as controler]
            [shouter.view.layout :as layout]
            [shouter.model.migration :as schema])
  (:gen-class))

(defroutes routes
  controler/routes
  (route/resources "/")
  (route/not-found (layout/glavni-template)))

(def application (handler/site routes))

(defn start [port]
  (run-jetty application {:port port
                          :join? false}))

(defn -main []
  (schema/migrate)
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (start port)))