(ns aikakonematka-puzzle-backend.util)

(def row-col-num 6)

(defn randomly-execute-a-fn
  ([f]
   (randomly-execute-a-fn f 0.5))
  ([f probability]
   (when (< (rand) probability) (f))))

(defn check-game-challenging-enough? [sprites-state]
  (let [{:keys [diagonal-flipped? row-flipped? col-flipped?]} @sprites-state
        true-count (+ (if diagonal-flipped? 1 0)
                      (count (filter val row-flipped?))
                      (count (filter val col-flipped?)))]
    (and (> true-count 3) (< true-count 8))))
