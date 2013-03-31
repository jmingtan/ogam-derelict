(ns marchgame.core
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer [defroutes GET]]
            [hiccup.element :refer [javascript-tag link-to]]
            [hiccup.page :refer [html5 include-js include-css]]))

(defn page-index []
  (html5
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html;charset=UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    (include-css
     "/css/bootstrap-combined.no-icons.min.css"
     "/css/font-awesome.css"
     "/css/font-awesome-ie7.css"
     "/css/site.css")
    (javascript-tag "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
  (i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
  m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
  })(window,document,'script','//www.google-analytics.com/analytics.js','ga');

  ga('create', 'UA-37871421-3', 'eugene-tan.info');
  ga('send', 'pageview');")
    [:title "Derelict"]]
   [:body {:onload "marchgame.core.init();"}
    [:div.container
     [:div.row
      [:div#body.span12
       [:h1 "Derelict"]]]
     [:div.row
      [:div.span3
       [:table
        (map (fn [[l m r]] [:tr
                            [:td {:align "right"} l]
                            [:td {:align "center"} m]
                            [:td r]])
             [[[:button#nw.btn "NW"] [:button#n.btn [:i.icon-caret-up]] [:button#ne.btn "NE"]]
              [[:button#w.btn [:i.icon-caret-left]] [:button#exit.btn [:i.icon-signin]] [:button#e.btn [:i.icon-caret-right]]]
              [[:button#sw.btn "SW"] [:button#s.btn [:i.icon-caret-down]] [:button#se.btn "SE"]]])]
       [:table#status
        [:tbody
         [:tr [:th "Ship"] [:td#ship "35/35"]]
         [:tr [:th "Player"] [:td#hp "20/20"]]
         [:tr [:th {:colspan 2} "Treasure"]]
         [:tr [:td#treasure {:colspan 2} "None"]]]]]
      [:div#message-span.span9
       [:table#messages]]]]
    (include-js "js/rot.js" "js/rogue.js" "js/typewriter.js")]))

(defroutes app-routes
  (GET "/" [] (page-index))
  (route/resources "/")
  (route/not-found "Page not found"))

(def handler
  (handler/site app-routes))
