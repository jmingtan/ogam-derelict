(ns marchgame.core
  (:use [marchgame.util :only (log get-location unpack-location)]
        [domina :only (by-id)])
  (:require [domina.events :as ev]
            [marchgame.path :as path]
            [marchgame.engine :as engine]
            [marchgame.entity :as entity]
            [marchgame.keycodes :as keycodes]
            [marchgame.mapping :as mapping]
            [marchgame.display :as display]))

(defn move [dx dy]
  (let [s (entity/get-entity :player)]
    (if (and (engine/locked?) (seq s))
      (let [new-x (+ dx (:x s))
            new-y (+ dy (:y s))
            new-s (assoc s :x new-x :y new-y)
            movable? (mapping/is-passable? new-x new-y)
            entities (entity/has-entity? new-x new-y)]
        (if movable?
          (do
            (if (and movable? (= (count entities) 0))
              (entity/modify-entity! :player new-s)
              (entity/attack-entity! :player (first (first entities))))
            (mapping/draw-current-map)
            (engine/unlock)))))))

(defn key-down [e]
  (let [[dx dy] (keycodes/get-direction e)]
    (if (not (or (nil? dx) (nil? dy)))
      (move dx dy))))

(defn register-handlers []
  (ev/listen! :keydown key-down)
  (doseq [[id [x y]] {"n" [0 -1] "s" [0 1] "e" [1 0] "w" [-1 0]
                      "ne" [1 -1] "nw" [-1 -1] "se" [1 1] "sw" [-1 1]}]
    (ev/listen! (by-id id) :click (fn [e] (move x y)))))

(defn create-player [x y]
  (entity/create-entity
   x y "@" "white"
   #(do (entity/draw-entity-by-id :player)
        (engine/lock))))

(defn create-pedro [x y]
  (entity/create-entity
   x y "P" "red"
   (fn []
     (let [{px :x py :y} (entity/get-entity :player)
           entity (entity/get-entity :pedro)
           finder (path/astar px py mapping/is-passable?)
           result-path (path/get-path finder (:x entity) (:y entity))
           [new-x new-y] (second result-path)
           new-e (assoc entity :x new-x :y new-y)]
       (if (> (count result-path) 2)
         (entity/modify-entity! :pedro new-e)
         (if (entity/attack-entity! :pedro :player)
           (engine/lock)))
       (entity/draw-entity-by-id :pedro)))))

(defn generate-entities [map-coll]
  (let [free-loc #(get-location (:free-cells map-coll))]
    (doseq [[k v] {:player create-player :pedro create-pedro}]
      (entity/add-entity! k (apply v (free-loc))))))

(defn place-loot [{map-data :map-data :as map-coll}]
  (let [free-loc #(get-location (:free-cells map-coll))
        chosen (free-loc)
        with-loot (assoc-in map-coll [:map-data chosen] :loot)]
    with-loot))

(defn ^:export init []
  (let [map-result (-> (mapping/generate-map) (place-loot))]
    (generate-entities map-result)
    (mapping/set-current-map! map-result)
    (.appendChild (by-id "body") (display/container))
    (mapping/draw-current-map)
    (doseq [[k v] (entity/get-entities)]
      (entity/draw-entity v))
    (register-handlers)
    (engine/start)))
