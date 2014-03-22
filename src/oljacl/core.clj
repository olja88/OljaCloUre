(ns oljacl.core
  (:use [ring.adapter.jetty :as ring])        
  (:require [oljacl.controller.misc :as misc]
            [oljacl.controller.users :as users :refer (users)]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [compojure.core :as compojure :refer (GET POST ANY defroutes)]
            (compojure [handler :as handler]
                       [route :as route])
            [ring.util.response :as resp]
            [hiccup.page :as h]
            [hiccup.element :as e]
            [bultitude.core :as b]
            [oljacl.model.migration :as schema]))

(def login-form
  [:div {:class "row"}
   [:div {:class "columns small-12"}
    [:h3 "Login"]
    [:div {:class "row"}
     [:form {:method "POST" :action "login" :class "columns small-4"}
      [:div "Username" [:input {:type "text" :name "username"}]]
      [:div "Password" [:input {:type "password" :name "password"}]]
      [:div [:input {:type "submit" :class "button" :value "Login"}]]]]]])

(compojure/defroutes routes
  (GET "/" req
    (h/html5
      misc/pretty-head
      (misc/pretty-body
       (misc/github-link req)
       [:h2 "Interactive form authentication"]
       [:p "This app demonstrates typical username/password authentication, and a pinch of Friend's authorization capabilities."]
       [:h3 "Current Status " [:small "(this will change when you log in/out)"]]
       [:p (if-let [identity (friend/identity req)]
             (apply str "Logged in, with these roles: "
               (-> identity friend/current-authentication :roles))
             "anonymous user")]
       login-form
       [:h3 "Authorization demos"]
       [:p "Each of these links require particular roles (or, any authentication) to access. "
           "If you're not authenticated, you will be redirected to a dedicated login page. "
           "If you're already authenticated, but do not meet the authorization requirements "
           "(e.g. you don't have the proper role), then you'll get an Unauthorized HTTP response."]
       [:ul [:li (e/link-to (misc/context-uri req "role-user") "Requires the `user` role")]
        [:li (e/link-to (misc/context-uri req "role-admin") "Requires the `admin` role")]
        [:li (e/link-to (misc/context-uri req "requires-authentication")
               "Requires any authentication, no specific role requirement")]]
       [:h3 "Logging out"]
       [:p (e/link-to (misc/context-uri req "logout") "Click here to log out") "."])))
  (GET "/login" req
    (h/html5 misc/pretty-head (misc/pretty-body login-form)))
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))
  (GET "/requires-authentication" req
    (friend/authenticated "Thanks for authenticating!"))
  (GET "/role-user" req
    (friend/authorize #{::users/user} "You're a user!"))
  (GET "/role-admin" req
    (friend/authorize #{::users/admin} "You're an admin!")))

(def page (handler/site
            (friend/authenticate
              routes
              {:allow-anon? true
               :login-uri "/login"
               :default-landing-uri "/"
               :unauthorized-handler #(-> (h/html5 [:h2 "You do not have sufficient privileges to access " (:uri %)])
                                        resp/response
                                        (resp/status 401))
               :credential-fn #(creds/bcrypt-credential-fn @users %)
               :workflows [(workflows/interactive-form)]})))
                        

(defn- wrap-app-metadata
  [h app-metadata]
  (fn [req] (h (assoc req :demo app-metadata))))

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

(def erp (apply compojure/routes
           page
           (route/resources "/" {:root "META-INF/resources/webjars/foundation/5.1.1/"})
           (for [{:keys [app page route-prefix] :as metadata} the-menagerie]
             (compojure/context route-prefix []
               (wrap-app-metadata (compojure/routes (or page (fn [_])) (or app (fn [_]))) metadata)))))



(defroutes landing
  (GET "/" req (h/html5 [:html
                         misc/pretty-head
                         (misc/pretty-body 
                          [:h1 {:style "margin-bottom:0px"}
                           [:a {:href "http://github.com/cemerick/friend-demo"} "Among Friends"]]
                          [:p {:style "margin-top:0px"} "…a collection of demonstration apps using "
                           (e/link-to "http://github.com/cemerick/friend" "Friend")
                           ", an authentication and authorization library for securing Clojure web services and applications."]
                          [:p "Implementing authentication and authorization for your web apps is generally a
necessary but not particularly pleasant task, even if you are using Clojure.
Friend makes it relatively easy and relatively painless, but I thought the
examples that the project's documentation demanded deserved a better forum than
to bit-rot in a markdown file or somesuch. So, what better than a bunch of live
demos of each authentication workflow that Friend supports (or is available via
another library that builds on top of Friend), with smatterings of
authorization examples here and there, all with links to the
generally-less-than-10-lines of code that makes it happen?  

Check out the demos, find the one(s) that apply to your situation, and
click the button on the right to go straight to the source for that demo:"]
                          [:div {:class "columns small-8"}
                           [:h2 "Demonstrations"]
                           [:ol
                            (for [{:keys [name doc route-prefix]} the-menagerie]
                              [:li (e/link-to (str route-prefix "/") [:strong name])
                               " — " doc])]]
                          [:div {:class "columns small-4"}
                           [:h2 "Credentials"]
                           [:p "All demo applications here that directly require user-provided credentials
recognize two different username/password combinations:"]
                           [:ul [:li [:code "friend/clojure"] " — associated with a \"user\" role"]
                                [:li [:code "friend-admin/clojure"] " — associated with an \"admin\" role"]]])])))



(defn start [port]
  (run-jetty erp {:port port
                          :join? false}))

;;(def application (handler/site routes))

(defn -main []
  (schema/migrate)
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (start port)))