(ns marchgame.core
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer [defroutes GET]]
            [hiccup.element :refer [javascript-tag]]
            [hiccup.page :refer [html5 include-js]]))

(defn page-index []
  (html5
   [:head
    [:meta {:http-equiv "Content-Type" :content "text/html;charset=UTF-8"}]
    [:title "Derelict"]]
   [:body {:onload "marchgame.core.init();"}
    (include-js "js/rot.js" "js/rogue.js" "js/game.js")]))

(defroutes app-routes
  (GET "/" [] (page-index))
  (route/resources "/")
  (route/not-found "Page not found"))

(def handler
  (handler/site app-routes))
