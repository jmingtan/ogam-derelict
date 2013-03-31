(ns marchgame.util
  (:use [domina :only (by-id append!)]))

(defn log [& elems]
  (let [line (clojure.string/join " " elems)
        msg-elem (by-id "messages")
        msg-container (by-id "message-span")]
    (.log js/console line)
    (append! msg-elem (format "<tr><td>%s</td></tr>" line))
    (set! (.-scrollTop msg-container) (.-scrollHeight msg-container))))

(defn timed-log [& elems]
  (let [line (clojure.string/join " " elems)
        msg-elem (by-id "messages")
        msg-container (by-id "message-span")]
    (.log js/console line)
    (js/displayText "messages" line)
    (set! (.-scrollTop msg-container) (.-scrollHeight msg-container))))

(defn rrand-int [n]
  (-> (js/ROT.RNG.getUniform)
      (* n)
      (Math/floor)))

(defn get-location [free-cells]
  (let [index (rrand-int (count free-cells))]
    (get (vec (keys free-cells)) index)))

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
