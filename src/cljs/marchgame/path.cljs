(ns marchgame.path
  (:use [marchgame.util :only (log)]))

(def cache (atom {:x nil :y nil :f nil}))

(defn astar
  ([target-x target-y passable-fn]
     (astar target-x target-y passable-fn 8))
  ([target-x target-y passable-fn topology]
     (let [{x :x y :y f :f} @cache]
       (if (and (= x target-x) (= y target-y) (fn? f))
         f
         (let [opts (js-obj "topology" topology)
               finder (js/ROT.Path.AStar.
                       target-x target-y passable-fn opts)]
           (:f (swap! cache assoc :x target-x :y target-y :f finder)))))))

(defn compute [finder source-x source-y callback]
  (.compute finder source-x source-y callback))

(defn get-path [finder source-x source-y]
  (let [result (atom [])
        callback (fn [x y] (swap! result conj [x y]))]
    (compute finder source-x source-y callback)
    @result))
