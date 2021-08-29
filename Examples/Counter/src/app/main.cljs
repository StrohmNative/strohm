(ns app.main
  (:require [strohm-native.flow :refer [create-store]]
            [strohm-native.tx :refer [send-props!]]
            [strohm-native.log :as log]
            [clojure.string :as str]))

(defn reducer 
  [state action]
  (log/debug "reduce:" action (:type action) ((comp :count :payload) action))
  (case (:type action)
    "decrement" (dec state)
    "increment" (inc state)
    "setCounter" ((comp :count :payload) action)
    state))

(defn ^:export main! []
  (log/set-log-level! :debug)
  (create-store reducer :initial-state 0)
  (log/debug "[main] started"))

(defn sendTestMessage []
  (let [time ((juxt #(.getHours %) #(.getMinutes %) #(.getSeconds %)) (js/Date.))]
    (log/debug "send test message"
         (str/join ":" time))
    (send-props! (random-uuid) {:old :props} {:foo :bar :when time})))

(defn ^:export init []
  (js/setTimeout sendTestMessage 3000)
  (log/debug "init done"))

(defn ^:dev/after-load reload! []
  (log/debug "[main] reloaded"))
