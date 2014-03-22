(ns oljacl.view.layout
  (:use [hiccup.core :only (html)]
        [hiccup.page :only (html5 include-css include-js)]))

(defn common [title & body]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
    [:meta {:name "author" :content "Olja Latinović"}]
    [:meta {:name "keywords" :content "Olja Latinović, Clojure, Jquery"}]
    [:title title]
    (include-css "/stylesheets/base.css"
                 "/stylesheets/skeleton.css"
                 "/stylesheets/screen.css")
    (include-css "http://fonts.googleapis.com/css?family=Sigmar+One&v1")
    ;;(include-css "/css/bootstrap.min.css") 
   ]
   [:body
    [:header {:id "header"}
    [:h1 {:class "container"} "Olja Clojure"]]
    [:div {:id "content" :class "container"} body]
   
   [:footer{:id "container"}
           [:h1 {:class "container"} "&copy; Olja Latinović - 2014. "]]
    ;;(include-js "/js/jquery-2.1.0.min.js")
    ;;(include-js "/js/bootstrap.min.js")      
   ]
   )
 )

(defn err404-template []
  (common "Zalutali ste"
          [:div {:id "err404-template"}
          [:h2 {:class "container"} "Verovatno ste pogrešili adresu, na ovoj nema ništa!"]]))