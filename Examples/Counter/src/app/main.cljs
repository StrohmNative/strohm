(ns app.main
  (:require [strohm.store :refer [create-store]]
            [strohm.tx :refer [js->native]]
            [strohm.debug :as debug]
            [clojure.string :as str]))

(defn reducer 
  [state action]
  (debug/log "reduce:" action (:type action) ((comp :count :payload) action))
  (case (:type action)
    "decrement" (dec state)
    "increment" (inc state)
    "setCounter" ((comp :count :payload) action)
    state))

(defn ^:export main! []
  (create-store reducer :initial-state 0)
  (println "[main] started"))

(defn sendTestMessage []
  (let [time ((juxt #(.getHours %) #(.getMinutes %) #(.getSeconds %)) (js/Date.))]
    (debug/log "send test message"
         (str/join ":" time))
    (js->native {:foo :bar
                 :when time})))

(defn ^:export init []
  (js/setTimeout sendTestMessage 3000)
  (debug/log "init done"))

(defn ^:dev/after-load reload! []
  (println "[main] reloaded"))
