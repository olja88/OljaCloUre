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

(defn- wrap-app-metadata
  [h app-metadata]
  (fn [req] (h (assoc req :demo app-metadata))))

(def erp (apply compojure/routes
           landing
           (route/resources "/" {:root "META-INF/resources/webjars/foundation/5.1.1/"})
           (for [{:keys [app page route-prefix] :as metadata} the-menagerie]
             (compojure/context route-prefix []
               (wrap-app-metadata (compojure/routes (or page (fn [_])) (or app (fn [_]))) metadata)))))

(defn start [port]
  (run-jetty application {:port port
                          :join? false}))

(defn -main []
  (schema/migrate)
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (start port)))