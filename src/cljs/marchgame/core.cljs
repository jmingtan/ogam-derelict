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
  (let [[dx dy] (keycodes/get-direction e)
        s (entity/get-entity :player)]
    (if (not (or (nil? dx) (nil? dy)) (nil? s))
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
       (if (> (count result-path) 2)
         (entity/modify-entity! :pedro new-e)
         ;; (if (entity/attack-entity! :pedro :player)
         ;;   (engine/lock))
         )
       (entity/draw-entity-by-id :pedro)))))

(defn generate-entities [map-coll]
  (let [free-loc #(get-location (:free-cells map-coll))]
    (doseq [[k v] {:player create-player :pedro create-pedro}]
      (entity/add-entity! k (apply v (free-loc))))))

(defn ^:export init []
  (let [map-result (-> (mapping/generate-map) (mapping/generate-map-features))]
    (generate-entities map-result)
    (mapping/set-current-map! map-result)
    (.appendChild (by-id "body") (display/container))
    (mapping/draw-current-map)
    (doseq [[k v] (entity/get-entities)]
      (entity/draw-entity v))
    (engine/start)))
