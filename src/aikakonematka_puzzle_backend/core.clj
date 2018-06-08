(ns aikakonematka-puzzle-backend.core
  (:require [aikakonematka-puzzle-backend.game :as game]
            [compojure.core :refer (defroutes GET POST)]
            [cheshire.core :as json]
            [environ.core :refer [env]]
            [java-time :as jt]
            [org.httpkit.server :as server]
            [ring.middleware.defaults :as defaults]
            [ring.middleware.cors :as cors]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [aikakonematka-puzzle-backend.util :as util]))

(let [{:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      (sente/make-channel-socket!
        (get-sch-adapter)
        {:user-id-fn (fn [req] (get-in req [:params :client-id]))})]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def connected-uids connected-uids))

(def sprites-state (ref nil))

(def ranking (ref []))

(def game-start-time (ref nil))

(def sending-time-future (ref nil))

(def bgm-pitches (ref []))

(defn- convert-to-millis [seconds nanos]
  (+ (* 1000 seconds) (/ nanos 1000000)))

(defn- start-sending-current-playtime! []
  (future (loop []
            (Thread/sleep 200)
            (when-let [start-time @game-start-time]
              (let [duration (jt/duration start-time (jt/local-date-time))
                    seconds (jt/value (jt/property duration :seconds))
                    nanos (jt/value (jt/property duration :nanos))]
                (doseq [uid (:any @connected-uids)]
                  (chsk-send! uid [:aikakone/current-time (convert-to-millis seconds nanos)]))
                (recur))))))

(defn broadcast-data-to-all-except-msg-sender [client-id msg-type data]
  (doseq [uid (:any @connected-uids)]
    ; -listed by the connected uuids variable.
    (when (not= client-id uid)
      (chsk-send! uid [msg-type data]))))

(defn handle-event-message! [{:keys [id client-id ?data event]}]
  (case id
    :aikakone/sprites-state
    ; To identify type of msg and handle them accordingly
    ; To have unique UUID for each client that matches the ID used by the :user-id-fn
    ; To broadcast the response to all the connected clients
    (dosync
      (ref-set sprites-state ?data)
      (broadcast-data-to-all-except-msg-sender client-id :aikakone/sprites-state @sprites-state))

    :aikakone/game-start
    (dosync
      (when (empty? @sprites-state)
        (while (not (util/check-game-challenging-enough? sprites-state))
          (game/randomize-puzzle-pieces sprites-state)))
      (chsk-send! client-id [:aikakone/game-start @sprites-state]))

    :aikakone/start-timer
    (dosync
      (ref-set game-start-time (jt/local-date-time))
      (ref-set sending-time-future (start-sending-current-playtime!)))

    :aikakone/puzzle-complete!
    (dosync
      (ref-set sprites-state nil)
      (ref-set bgm-pitches nil)
      ;It will only take the first player's play time in each game
      (when @game-start-time
        (ref-set game-start-time nil)
        (alter ranking (fn [ranking]
                         (take 10 (sort (conj ranking ?data))))))
      (broadcast-data-to-all-except-msg-sender client-id :aikakone/sprites-state {}))

    :aikakone/reset
    (dosync
      (ref-set game-start-time nil)
      (ref-set sprites-state nil)
      (ref-set bgm-pitches nil)
      (broadcast-data-to-all-except-msg-sender client-id :aikakone/reset nil))

    :aikakone/music
    (dosync
      (alter bgm-pitches (fn [background-music]
                           (conj background-music ?data)))
      (println "bgm-pitches : " @bgm-pitches)
      (doseq [uid (:any @connected-uids)]
        (chsk-send! uid [:aikakone/music @bgm-pitches])))

    :default
    (println "Unhandled event: " event)))

(sente/start-chsk-router! ch-chsk handle-event-message!)        ; To initialize the router which uses core.async go-loop
; to manage msg routing between clients
; and pass it handle-message! as the event handler.

(defroutes app
           ;Make JSON with ranking data
           ;and open the link(localhost:2222/rankings for the clients
           (GET "/rankings" req (json/generate-string @ranking))
           (GET "/chsk" req (ring-ajax-get-or-ws-handshake req)) ; To update the routes with these two fns
           (POST "/chsk" req (ring-ajax-post req)))         ; to handle client requests.

(def handler
  (-> #'app
      (defaults/wrap-defaults (assoc-in defaults/site-defaults [:security :anti-forgery] false))
      (cors/wrap-cors :access-control-allow-origin [#".*"]
                      :access-control-allow-methods [:get :put :post :delete]
                      :access-control-allow-credentials ["true"])))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 2222))]
    (server/run-server handler {:port port :join? false})))
