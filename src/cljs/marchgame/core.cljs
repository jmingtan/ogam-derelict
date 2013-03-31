(ns marchgame.core
  (:use [marchgame.util :only (log get-location unpack-location)]
        [domina :only (by-id)])
  (:require [domina.events :as ev]
            [marchgame.path :as path]
            [marchgame.loot :as loot]
            [marchgame.engine :as engine]
            [marchgame.entity :as entity]
            [marchgame.keycodes :as keycodes]
            [marchgame.mapping :as mapping]
            [marchgame.display :as display]))

(def history (atom nil))

(defn generate-entities [map-coll]
  (let [free-loc #(get-location (:free-cells map-coll))]
    (doseq [[k v] {:player entity/create-player
                   :pedro entity/create-pedro}]
      (entity/add-entity! k (apply v (free-loc))))))

(defn place-elem [{map-data :map-data :as map-coll} elem]
  (let [free-loc #(get-location (:free-cells map-coll))
        chosen (free-loc)
        with-elem (assoc-in map-coll [:map-data chosen] elem)]
    with-elem))

(defn overhead-map []
  (let [map-result (-> (mapping/generate-map)
                       (place-elem :loot)
                       (place-elem :exit))]
    (generate-entities map-result)
    (mapping/set-current-map! map-result)
    (mapping/draw-current-map)
    (doseq [[k v] (entity/get-entities)]
      (entity/draw-entity v))))

(defn derelict-map []
  (let [map-result (-> (mapping/generate-map :uniform)
                       (place-elem :loot)
                       (place-elem :exit))]
    (generate-entities map-result)
    (mapping/set-current-map! map-result)
    (mapping/draw-current-map)
    (doseq [[k v] (entity/get-entities)]
      (entity/draw-entity v))))

(defn move [dx dy]
  (let [s (entity/get-entity :player)]
    (if (and (engine/locked?) (seq s))
      (let [new-x (+ dx (:x s))
            new-y (+ dy (:y s))
            new-s (assoc s :x new-x :y new-y)
            dest-cell (mapping/get-cell new-x new-y)
            movable? (mapping/is-passable? new-x new-y)
            entities (entity/has-entity? new-x new-y)]
        (cond
         (> (count entities) 0) (entity/attack-entity!
                                 :player (first (first entities)))
         (= :loot dest-cell) (do (loot/random-loot!)
                                 (mapping/set-cell! new-x new-y :floor)
                                 (entity/modify-entity! :player new-s))
         movable? (entity/modify-entity! :player new-s))
        (if movable?
          (do (mapping/draw-current-map)
              (engine/unlock))
          (log "You cannot go that way."))))))

(defn exit []
  (let [{x :x y :y :as s} (entity/get-entity :player)
        current-cell (mapping/get-cell x y)
        on-exit? (= current-cell :exit)
        h @history]
    (if on-exit?
      (if (nil? h)
        (do
          (log "Entering derelict...")
          (mapping/set-cell! x y :floor)
          (reset! history {:entities (entity/get-entities)
                           :map (mapping/get-current-map)})
          (entity/clear-entities!)
          (derelict-map))
        (do
          (log "Exiting to sector...")
          (entity/clear-entities!)
          (mapping/set-current-map! (:map h))
          (mapping/draw-current-map)
          (doseq [[k v] (:entities h)]
            (entity/add-entity! k v)
            (entity/draw-entity v))
          (reset! history nil)
          (engine/unlock))))))

(defn key-down [e]
  (let [[dx dy] (keycodes/get-direction e)
        keycode (:keyCode e)]
    (cond
     (= keycode 190) (exit)
     (not (or (nil? dx) (nil? dy))) (move dx dy))))

(defn register-handlers []
  (ev/listen! :keydown key-down)
  (ev/listen! (by-id "exit") :click (fn [e] (exit)))
  (doseq [[id [x y]] {"n" [0 -1] "s" [0 1] "e" [1 0] "w" [-1 0]
                      "ne" [1 -1] "nw" [-1 -1] "se" [1 1] "sw" [-1 1]}]
    (ev/listen! (by-id id) :click (fn [e] (move x y)))))

(defn ^:export init []
  (.appendChild (by-id "body") (display/container))
  (overhead-map)
  (register-handlers)
  (engine/start))
