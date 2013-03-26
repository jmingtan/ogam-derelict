(ns marchgame.engine)

(def lock-atom (atom 0))

(def engine (js/ROT.Engine.))

(defn locked? []
  (= @lock-atom 0))

(defn start []
  (.start engine))

(defn add-actor [actor]
  (.addActor engine actor))

(defn remove-actor [actor]
  (.removeActor engine actor))

(defn lock []
  (reset! lock-atom 0)
  (.lock engine))

(defn unlock []
  (reset! lock-atom 1)
  (.unlock engine))
