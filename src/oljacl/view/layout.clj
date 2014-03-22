(ns oljacl.view.layout
  (:use [hiccup.core :only (html)]
        [hiccup.page :only (html5 include-css)]))

(defn common [title & body]
  (html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
    [:meta {:name "author" :content "Olja Latinović"}]
    [:meta {:name "keywords" :content "Olja Latinović, Clojure"}]
    [:title title]
    (include-css "/css/bootstrap.min.css")
    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->    
    ]
   [:body
    [:header {:id "header"}
    [:h1 {:class "container"} "Olja Clojure"]]
    [:div {:id "content" :class "container"} body]
   
   [:footer{:id "footer"}
           [:p "&copy; Olja Latinović - 2014. "]]]
   
   )
  
 )

(defn err404-template []
  (common "Zalutali ste"
          [:div {:id "err404-template"}
          [:h2 {:class "container"} "Verovatno ste pogrešili adresu, na ovoj nema ništa!"]]))