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
         [:tr [:th "Player"] [:td#hp "20/20"]]]]]
      [:div#message-span.span9
       [:table#messages]]]]
    (include-js "js/rot.js" "js/rogue.js")]))

(defroutes app-routes
  (GET "/" [] (page-index))
  (route/resources "/")
  (route/not-found "Page not found"))

(def handler
  (handler/site app-routes))
