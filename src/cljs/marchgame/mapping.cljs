(ns marchgame.mapping
  (:use [marchgame.util :only (rrand-int log)])
  (:require [marchgame.display :as display]))

(def map-legend
  {:wall {:symbol "#" :colour "brown" :passable? false}
   :floor {:symbol " " :colour "brown" :passable? true}
   :aexit {:symbol ">" :colour "purple" :bg "white" :passable? true}
   :artifact {:symbol "*" :colour "purple" :bg "white" :passable? true}
   :warp {:symbol "O" :colour "black" :bg "white" :passable? true}
   :exit {:symbol ">" :colour "white" :passable? true}
   :loot {:symbol "$" :colour "yellow" :passable? true}})

(def current-map (atom {}))

(defn get-symbol [id]
  (-> map-legend id :symbol))

(defn optimize-map-data [{map-data :map-data}]
  (apply array (map (fn [[[x y] v]] [x y v]) map-data)))

(defn set-current-map! [new-map]
  (reset! current-map (assoc new-map :_optimized (optimize-map-data new-map))))

(defn get-current-map []
  @current-map)

(defn get-elem [x y]
  ((:map-data @current-map) [x y]))

(defn get-random-room [rooms]
  (let [index (rrand-int (count rooms))
        room (aget rooms index)]
    room))

(defn get-room-center [room]
  (let [center (.getCenter room)]
    [(aget center 0) (aget center 1)]))

(defn generate-cellular-map [callback]
  (let [g (js/ROT.Map.Cellular.)]
    (.randomize g 0.37)
    (dotimes [i 2]
      (.create g (fn [& rest])))
    (.create g callback)))

(defn generate-uniform-map [callback]
  (let [g (js/ROT.Map.Uniform.)]
    (.create g callback)))

(defn generate-divided-map [callback]
  (let [g (js/ROT.Map.DividedMaze.)]
    (.create g callback)))

(defn generate-map
  ([] (generate-map :cellular))
  ([map-type]
     (let [result-map (atom {})
           free-cells (atom {})
           callback (fn [x y value]
                      (let [is-wall? (= value 1)]
                        (if (not is-wall?) (swap! free-cells
                                                  assoc [x y] nil))
                        (swap! result-map
                               assoc [x y] (if is-wall?
                                             :wall
                                             :floor))))]
       (condp = map-type
         :cellular (generate-cellular-map callback)
         :uniform (generate-uniform-map callback)
         :divided (generate-divided-map callback)
         nil)
       {:map-data @result-map :free-cells @free-cells})))

(defn draw-map [map-data]
  (doseq [[x y v] map-data]
    (let [{s :symbol c :colour b :bg} (v map-legend)]
      (display/draw x y s c b))))

(defn draw-current-map []
  (draw-map (:_optimized @current-map)))

(defn is-passable? [x y]
  (:passable? (map-legend (get-elem x y))))

(defn get-cell [x y]
  ((:map-data @current-map) [x y]))

(defn set-cell! [x y value]
  (set-current-map! (assoc-in @current-map [:map-data [x y]] value)))
