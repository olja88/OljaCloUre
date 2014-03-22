(ns oljacl.core
  (:use [ring.adapter.jetty :as ring])
  (:require (compojure handler [route :as route])
            [compojure.core :as compojure :refer (GET defroutes)]
            [hiccup.core :as h]
            [hiccup.element :as e]
            [ring.middleware.resource :refer (wrap-resource)]
            ring.adapter.jetty
            [bultitude.core :as b]
            [oljacl.controller.misc :as misc]
            [oljacl.model.migration :as schema])
  (:gen-class))

(defn- erp-vars
  [ns]
  {:namespace ns
   :ns-name (ns-name ns)
   :name (-> ns meta :name)
   :doc (-> ns meta :doc)
   :route-prefix (misc/ns->context ns)
   :app (ns-resolve ns 'app)
   :page (ns-resolve ns 'page)})

(def the-menagerie (->> (b/namespaces-on-classpath :prefix misc/ns-prefix)
                     distinct
                     (map #(do (require %) (the-ns %)))
                     (map erp-vars)
                     (filter #(or (:app %) (:page %)))
                     (sort-by :ns-name)))

(defroutes landing
  (GET "/" req (h/html [:html
                        misc/pretty-head
                        (misc/pretty-body 
                         [:h1 {:style "margin-bottom:0px"}                         
                         [:div {:class "columns small-8"}
                          [:h2 "Olja test erp"]
                          [:ol
                           (for [{:keys [name doc route-prefix]} the-menagerie]
                             [:li (e/link-to (str route-prefix "/") [:strong name])
                              " — " doc])]]]
                         [:div {:class "columns small-4"}
                          [:h2 "Credentials"]
                          [:p "username/password"]])])))
                        

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
  (run-jetty erp {:port port
                          :join? false}))

(def application (handler/site routes))

(defn -main []
  (schema/migrate)
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (start port)))