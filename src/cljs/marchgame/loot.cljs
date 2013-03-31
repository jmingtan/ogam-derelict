(ns marchgame.loot
  (:use [marchgame.util :only (log timed-log)]
        [domina :only (by-id)]))

(def gold-name "scrap")
(def lootbag (atom {:gold 0 :artifact? false}))

(defn update-ui []
  (let [{gold :gold artifact? :artifact?} @lootbag
        elem (by-id "treasure")
        gold-line (format "%s %s" gold gold-name)
        final (if artifact?
                (format "%s, Artifact" gold-line)
                gold-line)]
    (set! (.-innerHTML elem) final)))

(defn add-loot! [item]
  (swap! lootbag assoc :gold (+ (:gold @lootbag) item))
  (update-ui))

(defn add-artifact! []
  (swap! lootbag assoc :artifact? true)
  (update-ui))

(defn random-loot! []
  (let [amt (rand-nth [5 10 15 20])]
    (log (format "Picked up %s %s." amt gold-name))
    (add-loot! amt)))

(defn calculate-score []
  (let [{gold :gold artifact? :artifact?} @lootbag
        gold-score (* 10 gold)
        artifact-score (if artifact? 1000 0)
        final-score (+ gold-score artifact-score)]
    (timed-log (format "Final score is %d."
                       final-score))))
