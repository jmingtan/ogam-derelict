(ns marchgame.path)

(defn astar
  ([target-x target-y passable-fn]
     (astar target-x target-y passable-fn 8))
  ([target-x target-y passable-fn topology]
     (let [opts (js-obj "topology" topology)
           finder (js/ROT.Path.AStar.
                   target-x target-y passable-fn opts)]
       finder)))

(defn compute [finder source-x source-y callback]
  (.compute finder source-x source-y callback))

(defn get-path [finder source-x source-y]
  (let [result (atom [])
        callback (fn [x y] (swap! result conj [x y]))]
    (compute finder source-x source-y callback)
    @result))
