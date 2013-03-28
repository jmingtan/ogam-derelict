(ns marchgame.mapping
  (:use [marchgame.util :only (rrand-int log)])
  (:require [marchgame.display :as display]))

(def map-legend
  {:wall {:symbol "#" :colour "brown" :passable? false}
   :floor {:symbol " " :colour "brown" :passable? true}
   :exit {:symbol ">" :colour "brown" :passable? true}
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

(defn generate-map []
  (let [g (js/ROT.Map.Cellular.)
        result-map (atom {})
        free-cells (atom {})
        callback (fn [x y value]
                   (let [is-wall? (= value 1)]
                     (if (not is-wall?) (swap! free-cells
                                               assoc [x y] nil))
                     (swap! result-map
                            assoc [x y] (if is-wall?
                                          :wall
                                          :floor))))]
    (.randomize g 0.3)
    (dotimes [i 2]
      (.create g (fn [& rest])))
    (.create g callback)
    {:map-data @result-map
     ;; :rooms (.getRooms g)
     :free-cells @free-cells}))

(defn generate-map-features [{map-data :map-data rooms :rooms :as map-coll}]
  (let [center-fn (comp get-room-center get-random-room)
        with-exit (assoc map-data (center-fn rooms) (get-symbol :exit))
        with-loot (assoc with-exit (center-fn rooms) (get-symbol :loot))]
    (assoc map-coll :map-data with-loot)))

(defn draw-map [map-data]
  (doseq [[x y v] map-data]
    (let [{s :symbol c :colour} (v map-legend)]
      (display/draw x y s c))))

(defn draw-current-map []
  (draw-map (:_optimized @current-map)))

(defn is-passable? [x y]
  (:passable? (map-legend (get-elem x y))))

(defn get-cell [x y]
  ((:map-data @current-map) [x y]))

(defn set-cell! [x y value]
  (set-current-map! (assoc-in @current-map [:map-data [x y]] value)))
