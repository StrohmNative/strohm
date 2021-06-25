(ns app.main
  (:require [strohm.native :refer [create-store combine-reducers clj->js']]
            [app.entries.reducer :as entries]
            [app.navigation.reducer :as navigation]
            [strohm.tx :as tx]
            [strohm.debug :as debug]))

(def reducer (combine-reducers {"entries" entries/reducer}))

(defn send-state! [state]
  (let [serialized-state (js/JSON.stringify (clj->js' state))]
    (tx/send-message! {:function "persistState"
                       :state serialized-state})))

(defn persist-state-middleware
  [next]
  (fn [store action]
    (let [next-store (next store action)]
      (send-state! (:state next-store))
      next-store)))

(def empty-store
  {"entries" entries/initial-state
   :navigation navigation/initial-state})

(defn- setup []
  (create-store reducer
                :initial-state empty-store
                :middlewares [persist-state-middleware]))

(defn ^:export main! []
  (debug/set-logging-enabled!)
  (setup)
  (js/console.debug "[main] started"))

(defn ^:export init []
  (js/console.debug "[main] init done"))

(defn ^:dev/after-load reload! []
  (debug/clear-log)
  (js/console.debug "[main] reloaded"))
