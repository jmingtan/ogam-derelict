(ns marchgame.keycodes)

(def keymap-codes
  {38 0
   33 1
   39 2
   34 3
   40 4
   35 5
   37 6
   36 7})

(defn get-direction [e]
  (let [code (:keyCode e)
        dir (aget (aget js/ROT.DIRS 8) (keymap-codes code))]
    [(aget dir 0) (aget dir 1)]))
