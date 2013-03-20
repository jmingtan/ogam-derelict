(ns marchgame.core
  (:use [marchgame.util :only (log get-location unpack-location)])
  (:require [domina.events :as ev]
            [marchgame.engine :as engine]
            [marchgame.display :as display]))

(def keymap
  {38 0
   33 1
   39 2
   34 3
   40 4
   35 5
   37 6
   36 7})

(def game (atom nil))

(defn generate-map []
  (let [g (js/ROT.Map.Uniform.)
        result-map (atom {})
        free-cells (atom [])
        callback (fn [x y value]
                   (let [is-wall? (= value 1)]
                     (if (not is-wall?) (swap! free-cells conj [x y]))
                     (swap! result-map
                            assoc [x y] (if is-wall? "#" " "))))]
    (.create g callback)
    {:map-data @result-map
     :free-cells @free-cells}))

(defn draw-map [map-data]
  (doseq [[[x y] v] map-data]
    (display/draw x y v)))

(defn draw-current-map []
  (draw-map (:map-data @game)))

(defn get-entity [id]
  (-> @game :entities id))

(defn draw-entity [entity]
  (apply display/draw (map #(% entity) [:x :y :symbol :colour])))

(defn draw-entity-by-id [id]
  (draw-entity (get-entity id)))

(defn key-down [e]
  (let [code (:keyCode e)
        dir (aget (aget js/ROT.DIRS 8) (keymap code))
        s (get-entity :player)
        new-x (+ (aget dir 0) (:x s))
        new-y (+ (aget dir 1) (:y s))
        movable? (some #{[new-x new-y]} (:free-cells @game))]
    (if movable?
      (let [new-s (assoc s :x new-x :y new-y)]
        (swap! game #(assoc-in % [:entities :player] new-s))
        (draw-current-map)
        (engine/unlock))
      (ev/listen-once! :keydown key-down))))

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

(defn create-player [x y]
  (create-entity x y "@" "white"
                 #(do (draw-entity-by-id :player)
                      (ev/listen-once! :keydown key-down)
                      (engine/lock))))

(defn create-pedro [x y]
  (create-entity x y "P" "red"
                 (fn [] (draw-entity-by-id :pedro))))

(defn ^:export init []
  (let [map-result (generate-map)
        free-loc #(get-location (:free-cells map-result))
        entities {:player (apply create-player (free-loc))
                  :pedro (apply create-pedro (free-loc))}]
    (reset! game (merge map-result
                        {:entities entities}))
    (.appendChild (.-body js/document) (display/container))
    (draw-current-map)
    (doseq [e (vals entities)]
      (draw-entity e)
      (engine/add-actor (:actor e)))
    (engine/start)))
