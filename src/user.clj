(ns user
  (:require [aikakonematka-puzzle-backend.core :as core]
            ))

(defonce server (atom nil))

(defn stop-server []
  (dosync
    (ref-set core/sprites-state nil)
    (ref-set core/ranking nil))
  (future-cancel @core/sending-time-future)
  (when-not (nil? server)
    ;; graceful shutdown; wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately.
    (@server :timeout 100)
    (reset! server nil)))

(defn start-server []
  (reset! server (core/-main)))
