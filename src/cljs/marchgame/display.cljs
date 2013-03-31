(ns marchgame.display)

(def display (js/ROT.Display.))

(defn container []
  (.getContainer display))

(defn draw
  ([x y sym]
     (.draw display x y sym))
  ([x y sym colour]
     (.draw display x y sym colour))
  ([x y sym colour bg]
     (.draw display x y sym colour bg)))
