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

(defn draw-all-entities []
  (doseq [id (keys @entities)]
    (draw-entity-by-id id)))

(defn create-entity
  ([x y symbol name act-fn]
     (create-entity x y symbol name "grey"))
  ([x y symbol name colour act-fn]
     (let [speed-fn (fn [] 100)]
       (create-entity x y symbol name colour speed-fn act-fn)))
  ([x y symbol name colour speed-fn act-fn]
     {:x x :y y :symbol symbol :colour colour :hp 10 :name name
      :actor (js-obj "getSpeed" speed-fn "act" act-fn)}))

(defn attack-entity! [src-id tgt-id]
  (let [[src tgt] (map get-entity [src-id tgt-id])
        [src-name tgt-name] (map :name [src tgt])
        dmg (rand-nth (range 5 8))
        tgt-hp (- (:hp tgt) dmg)]
    (log (format "%s attacks %s, dealing %d damage (hp left: %d)"
                 src-name tgt-name dmg tgt-hp))
    (if (<= tgt-hp 0)
      (do
        (log (str (name tgt-name) " has died!"))
        (remove-entity! tgt-id)
        true)
      (let [new-e (assoc tgt :hp tgt-hp)]
        (modify-entity! tgt-id new-e)
        false))))

(defn create-player [x y]
  (create-entity
   x y "@" "Player" "white"
   #(engine/lock)))

(defn enemy-logic [id radius]
  (let [{px :x py :y} (get-entity :player)
        {x :x y :y :as entity} (get-entity id)
        finder (path/astar px py mapping/is-passable?)
        result-path (path/get-path finder x y)
        [new-x new-y] (second result-path)
        new-e (assoc entity :x new-x :y new-y)
        path-len (count result-path)]
    (cond
     (and (pos? radius) (>= path-len radius)) nil
     (> path-len 2) (modify-entity! id new-e)
     (= path-len 2) (if (attack-entity! id :player)
                      (engine/lock)))))

(defn create-enemy
  ([id x y] (create-enemy id 0 x y))
  ([id radius x y]
     (create-entity
      x y "P" "Pirate" "red"
      (partial enemy-logic id radius))))
