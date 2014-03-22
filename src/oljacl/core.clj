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
            [hiccup.element :as e]))

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

(def erp (apply compojure/routes
           page
           (route/resources "/" {:root "META-INF/resources/webjars/foundation/5.1.1/"})
           (for [{:keys [app page route-prefix] :as metadata} the-menagerie]
             (compojure/context route-prefix []
               (wrap-app-metadata (compojure/routes (or page (fn [_])) (or app (fn [_]))) metadata)))))

(defn start [port]
  (run-jetty erp {:port port
                          :join? false}))

;;(def application (handler/site routes))

(defn -main []
  (schema/migrate)
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (start port)))