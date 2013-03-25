(ns marchgame.entity
  (:use [marchgame.util :only (log)])
  (:require [marchgame.display :as display]
            [marchgame.engine :as engine]))

(def entities (atom {}))

(defn get-entity [id]
  (id @entities))

(defn get-entities []
  @entities)

(defn modify-entity! [id entity]
  (swap! entities assoc id entity))

(defn add-entity! [id entity]
  (modify-entity! id entity)
  (engine/add-actor (:actor entity)))

(defn remove-entity! [id]
  (engine/remove-actor (:actor (get-entity id)))
  (swap! entities dissoc id))

(defn has-entity? [x y]
  (filter (fn [[k {kx :x ky :y}]] (if (and (= kx x) (= ky y)) k))
          @entities))

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
     {:x x :y y :symbol symbol :colour colour :hp 10
      :actor (js-obj "getSpeed" speed-fn
                     "act" act-fn)}))

(defn attack-entity! [src-id tgt-id]
  (let [[src tgt] (map get-entity [src-id tgt-id])
        tgt-hp (- (:hp tgt) 5)]
    (log (str src-id " has attacked " tgt-id " (hp left: " tgt-hp ")"))
    (if (<= tgt-hp 0)
      (do
        (log (str tgt-id " has died!"))
        (remove-entity! tgt-id)
        true)
      (let [new-e (assoc tgt :hp tgt-hp)]
        (modify-entity! tgt-id new-e)
        false))))
