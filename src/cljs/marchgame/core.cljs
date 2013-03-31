(ns marchgame.core
  (:use [marchgame.util :only (log timed-log get-location unpack-location)]
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
(def status (atom {}))

(defn update-health [key a old new]
  (let [update-fn (fn [hp-type]
                    (let [{hp :hp} (:player new)
                          hp (or hp 0)
                          kw (-> (format "orig-%s" (name hp-type)) keyword)
                          {orig kw} (swap! status assoc hp-type hp)
                          elem (by-id (name hp-type))
                          line (format "%d/%d" hp orig)]
                      (set! (.-innerHTML elem) line)
                      (if (= hp 0)
                        (loot/calculate-score))))]
    (if (nil? @history)
      (update-fn :ship)
      (update-fn :hp))))

(defn set-player-hp [hp]
  (entity/modify-entity!
   :player (assoc (entity/get-entity :player) :hp hp)))

(defn generate-entities [map-coll n]
  (let [free-loc #(get-location (:free-cells map-coll))
        pirates (reduce (fn [coll e] (let [k (keyword (format "pirate%d" e))]
                                       (assoc coll k (partial entity/create-enemy k))))
                        {} (range n))
        entities (assoc pirates :player entity/create-player)]
    (doseq [[k v] entities]
      (entity/add-entity! k (apply v (free-loc))))))

(defn place-elem [{map-data :map-data :as map-coll} elem]
  (let [free-loc #(get-location (:free-cells map-coll))
        chosen (free-loc)
        with-elem (assoc-in map-coll [:map-data chosen] elem)]
    with-elem))

(defn place-elems [coll n elem]
  (reduce place-elem coll (repeat n elem)))

(defn overhead-map []
  (let [map-result (-> (mapping/generate-map :cellular)
                       (place-elems (rand-nth (range 4 7)) :loot)
                       (place-elems 4 :exit)
                       (place-elem :aexit))]
    (generate-entities map-result 1)
    (mapping/set-current-map! map-result)
    (mapping/draw-current-map)
    (doseq [[k v] (entity/get-entities)]
      (entity/draw-entity v))))

(defn derelict-map []
  (let [map-result (-> (mapping/generate-map :uniform)
                       (place-elems (rand-nth (range 3 5)) :loot)
                       (place-elem :exit))]
    (generate-entities map-result 1)
    (mapping/set-current-map! map-result)
    (mapping/draw-current-map)
    (doseq [[k v] (entity/get-entities)]
      (entity/draw-entity v))))

(defn artifact-map []
  (let [map-result (-> (mapping/generate-map :divided)
                       (place-elems 10 :loot)
                       (place-elem :artifact))]
    (generate-entities map-result 1)
    (mapping/set-current-map! map-result)
    (mapping/draw-current-map)
    (doseq [[k v] (entity/get-entities)]
      (entity/draw-entity v))))

(defn exit []
  (let [{x :x y :y :as s} (entity/get-entity :player)
        current-cell (mapping/get-cell x y)
        h @history]
    (condp = current-cell
      :warp
      (do
        (timed-log "You escape from the sector, riches in your grasp!")
        (entity/unwatch-entities)
        (entity/remove-entity! :player)
        (loot/calculate-score))
      :exit
      (if (nil? h)
        (do
          (timed-log "Entering derelict...")
          (entity/unwatch-entities)
          (mapping/set-cell! x y :floor)
          (reset! history {:entities (entity/get-entities)
                           :map (mapping/get-current-map)})
          (entity/clear-entities!)
          (derelict-map)
          (set-player-hp (:hp @status))
          (entity/watch-entities update-health)
          (engine/unlock))
        (do
          (timed-log "Exiting to sector...")
          (entity/unwatch-entities)
          (entity/clear-entities!)
          (mapping/set-current-map! (:map h))
          (mapping/draw-current-map)
          (doseq [[k v] (:entities h)]
            (entity/add-entity! k v)
            (entity/draw-entity v))
          (reset! history nil)
          (set-player-hp (:ship @status))
          (entity/watch-entities update-health)))
      :aexit
      (do
        (timed-log "Entering anomaly...")
        (entity/unwatch-entities)
        (mapping/set-cell! x y :floor)
        (reset! history {:entities (entity/get-entities)
                         :map (mapping/get-current-map)})
        (entity/clear-entities!)
        (artifact-map)
        (set-player-hp (:hp @status))
        (entity/watch-entities update-health)
        (engine/unlock))
      nil)))

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
         (= :artifact dest-cell) (do (loot/add-artifact!)
                                     (timed-log "A mysterious energy surrounds you. You find yourself onboard your ship again.")
                                     (mapping/set-cell! new-x new-y :exit)
                                     (entity/modify-entity! :player new-s)
                                     (exit)
                                     (timed-log "Escape to the warp point!")
                                     (-> (mapping/get-current-map)
                                         (place-elem :warp)
                                         (mapping/set-current-map!)))
         movable? (entity/modify-entity! :player new-s))
        (if movable?
          (do (mapping/draw-current-map)
              (entity/draw-all-entities)
              (engine/unlock))
          (log "You cannot go that way."))))))

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
  (let [player-orig 20
        ship-orig 35]
    (reset! status {:orig-hp player-orig :hp player-orig
                    :orig-ship ship-orig :ship ship-orig
                    :artifact? false})
    (entity/modify-entity!
     :player (assoc (entity/get-entity :player) :hp ship-orig)))
  (entity/watch-entities update-health)
  (register-handlers)
  (timed-log "Scanning sector... |4 derelicts|1 anomalous signature|Multiple hostiles|")
  (engine/start))
