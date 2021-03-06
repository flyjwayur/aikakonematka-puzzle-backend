(ns aikakonematka-puzzle-backend.game
  (:require [aikakonematka-puzzle-backend.util :as util]))

(defn flip-diagonal-pieces! [sprites-state]
  (alter sprites-state update :diagonal-flipped? not))

(defn flip-row! [sprites-state row]
  (alter sprites-state update-in [:row-flipped? row] not))

(defn flip-col! [sprites-state col]
  (alter sprites-state update-in [:col-flipped? col] not))

(defn randomize-puzzle-pieces [sprites-state]
  (let [non-flipped-row-or-col (reduce #(assoc %1 %2 false)
                                       {}
                                       (range util/row-col-num))]
    (ref-set sprites-state {:diagonal-flipped? false
                            :row-flipped?      non-flipped-row-or-col
                            :col-flipped?      non-flipped-row-or-col}))
  (util/randomly-execute-a-fn (fn [] flip-diagonal-pieces! sprites-state) 0.9)
  (doseq [row-or-col (range util/row-col-num)]
    (util/randomly-execute-a-fn (fn [] (flip-row! sprites-state row-or-col)))
    (util/randomly-execute-a-fn (fn [] (flip-col! sprites-state row-or-col)))))
