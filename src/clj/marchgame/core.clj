(ns marchgame.core
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer [defroutes GET]]
            [hiccup.element :refer [javascript-tag link-to]]
            [hiccup.page :refer [html5 include-js]]))

(defn page-index []
  (html5
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html;charset=UTF-8"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:link {:rel "stylesheet" :href "//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-combined.min.css"}]
    [:title "Derelict"]]
   [:body {:onload "marchgame.core.init();"}
    [:div.container
     [:div.row
      [:div#body.span12
       [:h1 "Derelict"]]
      [:div.span12
       [:table
        (map (fn [[l m r]] [:tr [:td {:align "right"} l] [:td m] [:td r]])
             [[[:button#nw.btn "NW"] [:button#n.btn [:i.icon-arrow-up]] [:button#ne.btn "NE"]]
              [[:button#w.btn [:i.icon-arrow-left]] [:button#wait.btn [:i.icon-time]] [:button#e.btn [:i.icon-arrow-right]]]
              [[:button#sw.btn "SW"] [:button#s.btn [:i.icon-arrow-down]] [:button#se.btn "SE"]]])]]]]
    (include-js "js/rot.js" "js/rogue.js")]))

(defroutes app-routes
  (GET "/" [] (page-index))
  (route/resources "/")
  (route/not-found "Page not found"))

(def handler
  (handler/site app-routes))
