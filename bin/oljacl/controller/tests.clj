(ns oljacl.controller.tests
    (:use [compojure.core :only (defroutes GET POST)])
    (:require [clojure.string :as str]
              [ring.util.response :as ring]
              [oljacl.view.tests :as view]
              [oljacl.model.test :as model]))

(defn index []
  (view/index (model/all)))

(defn create
  [test]
  (when-not (str/blank? test)
    (model/create test))
  (ring/redirect "/"))

(defroutes routes
  (GET  "/" [] (index))
  (POST "/" [test] (create test)))

