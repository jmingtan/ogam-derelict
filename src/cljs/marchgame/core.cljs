(ns marchgame.core)

(defn log [s]
  (.log js/console s))

(defn get-location [free-cells]
  (-> (js/ROT.RNG.getUniform)
      (* (count free-cells))
      (Math/floor)))

(defn ^:export create-being [f free-cells]
  (let [index (get-location free-cells)
        key (first (.splice free-cells index 1))
        parts (.split key ",")
        x (js/parseInt (first parts))
        y (js/parseInt (second parts))]
    (f. x y)))

(defn generate-boxes [state free-cells]
  (dotimes [i 10]
    (let [index (get-location free-cells)
          key (first (.splice free-cells index 1))]
      (aset (.-map state) key "*")
      (if (= i 0) (set! (.-ananas state) key)))))

(defn ^:export draw-whole-map [state]
  (js/doseq (fn [k]
              (let [parts (.split k ",")
                    x (js/parseInt (first parts))
                    y (js/parseInt (second parts))]
                (.draw (.-display state) x y (aget (.-map state) k))))
    (.-map state)))

;; (defn ^:export generate-map []
;;   (let [digger (js/ROT.Map.Digger.)
;;         free-cells []
;;         dig-fn (fn [x y val]
;;                  (if (nil? val)
;;                    (let [k (str x "," y)
;;                          ])))]
;;     ))

(defn ^:export get-speed []
  100)

(deftype Player [x y display]
  Object
  (getSpeed [_] 100)
  (act [_])
  (draw [_]
    (.draw display x y "@" "white")))

(defn ^:export init []
  (let [display (js/ROT.Display.)
        generator (js/ROT.Map.Uniform.)
        main-map (js-obj)
        free-cells (atom [])
        state (js-obj "map" main-map "display" display)
        player (Player. 5 5 display)]
    (.appendChild (.-body js/document) (.getContainer display))
    (.create generator (fn [x y value]
                      (let [k (str x "," y)]
                        (if (= value 0)
                          (aset main-map k " ")
                          (aset main-map k "#"))
                        (swap! free-cells conj k))))
    (draw-whole-map state)
    (.draw player))
  ;; (.init js/Game)
  )
