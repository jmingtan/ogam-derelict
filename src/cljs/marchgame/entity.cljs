(ns marchgame.entity
  (:use [marchgame.util :only (log)]
        [domina :only (by-id)])
  (:require [marchgame.display :as display]
            [marchgame.engine :as engine]
            [marchgame.path :as path]
            [marchgame.mapping :as mapping]))

(def entities (atom {}))

(defn get-entity [id]
  (id @entities))

(defn get-entities []
  @entities)

(defn clear-entities! []
  (reset! entities {})
  (engine/clear-actors))

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

(defn watch-entities [watch-fn]
  (add-watch entities :watch watch-fn))

(defn unwatch-entities []
  (remove-watch entities :watch))

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
    (log (str (name src-id) " has attacked " (name tgt-id) " (hp left: " tgt-hp ")"))
    (if (<= tgt-hp 0)
      (do
        (log (str (name tgt-id) " has died!"))
        (remove-entity! tgt-id)
        true)
      (let [new-e (assoc tgt :hp tgt-hp)]
        (modify-entity! tgt-id new-e)
        false))))

(defn create-player [x y]
  (create-entity
   x y "@" "white"
   #(do (draw-entity-by-id :player)
        (engine/lock))))

(defn enemy-logic [id]
  (let [{px :x py :y} (get-entity :player)
        {x :x y :y :as entity} (get-entity id)
        finder (path/astar px py mapping/is-passable?)
        result-path (path/get-path finder x y)
        [new-x new-y] (second result-path)
        new-e (assoc entity :x new-x :y new-y)
        path-len (count result-path)]
    (cond
     (> path-len 2) (modify-entity! id new-e)
     (= path-len 2) (if (attack-entity! id :player)
                      (engine/lock)))
    (draw-entity-by-id id)))

(defn create-pedro [x y]
  (create-entity
   x y "P" "red"
   (partial enemy-logic :pedro)))
