(ns oljacl.view.tests
      (:use [hiccup.core :only (h)]
          [hiccup.form :only (form-to label text-area submit-button)])
    (:require [oljacl.view.layout :as layout]))

(defn test-form []
  [:div {:id "test-form" :class "sixteen columns alpha omega"}
   (form-to [:post "/"]
            (label "test" "Test Ajaxa") 
            (text-area "test")
            (submit-button "Izvrši"))])

(defn display-tests [tests]
  [:div {:class "tests sixteen columns alpha omega"}
   (map
    (fn [test] [:h2 {:class "test"} (h (:body test))])
    tests)])

(defn index [tests]
  (layout/common "Olja Clojure"
                 (test-form)
                 [:div {:class "clear"}]
                 (display-tests tests)))

