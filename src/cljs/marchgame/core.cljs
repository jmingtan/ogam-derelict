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

(defn key-down [e]
  (let [dir (keycodes/get-direction e)]
    (if (not (nil? dir))
      (let [s (entity/get-entity :player)
            new-x (+ (aget dir 0) (:x s))
            new-y (+ (aget dir 1) (:y s))
            movable? (mapping/is-passable? new-x new-y)]
        (if movable?
          (let [new-s (assoc s :x new-x :y new-y)]
            (entity/modify-entity :player new-s)
            (mapping/draw-current-map)
            (engine/unlock))
          (ev/listen-once! :keydown key-down)))
      (ev/listen-once! :keydown key-down))))

(defn create-player [x y]
  (entity/create-entity
   x y "@" "white"
   #(do (entity/draw-entity-by-id :player)
        (ev/listen-once! :keydown key-down)
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
       (entity/modify-entity :pedro new-e)
       (entity/draw-entity-by-id :pedro)))))

(defn generate-entities [map-coll]
  (let [free-loc #(get-location (:free-cells map-coll))]
    (entity/add-entity :player (apply create-player (free-loc)))
    (entity/add-entity :pedro (apply create-pedro (free-loc)))))

(defn ^:export init []
  (let [map-result (-> (mapping/generate-map) (mapping/generate-map-features))]
    (generate-entities map-result)
    (mapping/set-current-map map-result)
    (.appendChild (by-id "body") (display/container))
    (mapping/draw-current-map)
    (doseq [[k v] (entity/get-entities)]
      (entity/draw-entity v)
      (entity/add-entity k v))
    (engine/start)))
