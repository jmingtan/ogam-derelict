(ns marchgame.loot
  (:use [marchgame.util :only (log)]))

(def lootbag (atom []))

(defn add-loot! [item]
  (log "Picked up" item)
  (swap! lootbag conj item))

(defn random-loot! []
  (add-loot! (str (rand-nth [5 10 15 20]) " gold")))
