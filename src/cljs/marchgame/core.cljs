(ns marchgame.core
  (:use [marchgame.util :only (log get-location unpack-location)])
  (:require [domina.events :as ev]))

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

(defn generate-boxes [state free-cells]
  (dotimes [i 10]
    (let [index (get-location free-cells)
          key (first (.splice free-cells index 1))]
      (aset (.-map state) key "*")
      (if (= i 0) (set! (.-ananas state) key)))))

(defn draw-whole-map []
  (let [main-map (:map @game)]
    (js/doseq (fn [k]
                (let [parts (.split k ",")
                      x (js/parseInt (first parts))
                      y (js/parseInt (second parts))
                      d (:display @game)]
                  (.draw d x y (aget main-map k))))
      main-map)))

(defn generate-map []
  (let [generator (js/ROT.Map.Uniform.)
        main-map (js-obj)
        free-cells (atom [])]
    (.create generator (fn [x y value]
                         (let [k (str x "," y)]
                           (if (= value 0)
                             (do (aset main-map k " ")
                                 (swap! free-cells conj k))
                             (aset main-map k "#")))))
    {:map main-map
     :free-cells @free-cells}))

(defn get-free-location [free-cells]
  (-> free-cells get-location unpack-location))

(defn get-actor [id]
  (-> @game :entities id))

(defn draw-entity [id]
  (let [s (get-actor id)
        d (:display @game)]
    (apply #(.draw d %1 %2 %3 %4)
           (map #(% s) [:x :y :symbol :colour]))))

(defn key-down [e]
  (let [code (:keyCode e)
        dir (aget (aget js/ROT.DIRS 8) (keymap code))
        s (get-actor :player)
        new-s (assoc s
                :x (+ (aget dir 0) (:x s))
                :y (+ (aget dir 1) (:y s)))]
    (swap! game #(assoc-in % [:entities :player] new-s))
    (.unlock (:engine @game))))

(defn create-player [x y]
  {:speed 100
   :x x
   :y y
   :symbol "@"
   :colour "white"
   :actor (js-obj "getSpeed" #(:speed (get-actor :player))
                  "act" #(do (draw-entity :player)
                             (ev/listen-once! :keydown key-down)
                             (.lock (:engine @game))))})

(defn ^:export init []
  (let [display (js/ROT.Display.)
        engine (js/ROT.Engine.)
        map-result (generate-map)
        entities {:player (apply create-player (get-free-location
                                                (:free-cells map-result)))}]
    (reset! game (merge map-result
                        {:display display
                         :engine engine
                         :entities entities}))
    (.appendChild (.-body js/document) (.getContainer display))
    (draw-whole-map)
    (.addActor engine (:actor (get-actor :player)))
    (.start engine)))
