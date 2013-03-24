(ns marchgame.mapping
  (:require [marchgame.display :as display]))

(def map-legend
  {:wall "#"
   :floor " "
   :exit ">"})

(def current-map (atom {}))

(defn set-current-map [new-map]
  (reset! current-map new-map))

(defn get-current-map []
  @current-map)

(defn place-exit [rooms]
  (let [room (aget rooms 0)
        center (.getCenter room)]
    [(aget center 0) (aget center 1)]))

(defn generate-map []
  (let [g (js/ROT.Map.Uniform.)
        result-map (atom {})
        free-cells (atom {})
        callback (fn [x y value]
                   (let [is-wall? (= value 1)]
                     (if (not is-wall?) (swap! free-cells
                                               assoc [x y] nil))
                     (swap! result-map
                            assoc [x y] (if is-wall?
                                          (:wall map-legend)
                                          (:floor map-legend)))))]
    (.create g callback)
    {:map-data @result-map
     :rooms (.getRooms g)
     :free-cells @free-cells}))

(defn generate-map-features [map-coll]
  (let [rooms (:rooms map-coll)]
    (assoc-in map-coll [:map-data (place-exit rooms)] (:exit map-legend))))

(defn draw-map [map-data]
  (doseq [[[x y] v] map-data]
    (display/draw x y v)))

(defn draw-current-map []
  (draw-map (:map-data @current-map)))

(defn is-passable? [x y]
  (contains? (:free-cells @current-map) [x y]))
