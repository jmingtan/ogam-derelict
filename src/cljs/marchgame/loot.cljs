(ns marchgame.loot
  (:use [marchgame.util :only (log)]
        [domina :only (by-id)]))

(def gold-name "scrap")
(def lootbag (atom {:gold 0}))

(defn add-loot! [item]
  (let [gold (+ (:gold @lootbag) item)
        elem (by-id "treasure")
        gold-line (format "%s %s" gold gold-name)]
    (swap! lootbag assoc :gold gold)
    (set! (.-innerHTML elem) gold-line)))

(defn random-loot! []
  (let [amt (rand-nth [5 10 15 20])]
    (log (format "Picked up %s %s." amt gold-name))
    (add-loot! amt)))
