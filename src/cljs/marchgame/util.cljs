(ns marchgame.util)

(defn log [s]
  (.log js/console s))

(defn rrand-int [n]
  (-> (js/ROT.RNG.getUniform)
      (* n)
      (Math/floor)))

(defn get-location [free-cells]
  (-> (rrand-int (count free-cells))
      (free-cells)))

(defn unpack-location [l]
  (let [parts (.split l ",")
        x (js/parseInt (first parts))
        y (js/parseInt (second parts))]
    [x y]))

(defn remove-index
  "Remove the zero-indexed element of coll"
  [coll n]
  ((fn [[head tail]]
     (let [remaining (take (dec (count head)) head)]
       (concat remaining tail)))
   (split-at (inc n) coll)))
