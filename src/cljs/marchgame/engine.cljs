(ns marchgame.engine)

(def engine (js/ROT.Engine.))

(defn start []
  (.start engine))

(defn add-actor [actor]
  (.addActor engine actor))

(defn remove-actor [actor]
  (.removeActor engine actor))

(defn lock []
  (.lock engine))

(defn unlock []
  (.unlock engine))
