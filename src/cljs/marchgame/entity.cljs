(ns marchgame.entity
  (:require [marchgame.display :as display]
            [marchgame.engine :as engine]))

(def entities (atom {}))

(defn add-entity [id entity]
  (swap! entities assoc id entity)
  (engine/add-actor (:actor entity)))

(defn get-entity [id]
  (id @entities))

(defn get-entities []
  @entities)

(defn modify-entity [id entity]
  (swap! entities assoc id entity))

(defn draw-entity [entity]
  (apply display/draw (map #(% entity) [:x :y :symbol :colour])))

(defn draw-entity-by-id [id]
  (draw-entity (get-entity id)))

(defn create-entity
  ([x y symbol act-fn]
     (create-entity x y symbol "grey"))
  ([x y symbol colour act-fn]
     (let [speed-fn (fn [] 100)]
       (create-entity x y symbol colour speed-fn act-fn)))
  ([x y symbol colour speed-fn act-fn]
     {:x x :y y :symbol symbol :colour colour
      :actor (js-obj "getSpeed" speed-fn
                     "act" act-fn)}))
