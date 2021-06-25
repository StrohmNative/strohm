(ns app.main
  (:require [strohm.native :refer [create-store combine-reducers]]
            [app.entries.reducer :as entries]
            [app.navigation.reducer :as navigation]))

(def reducer (combine-reducers {"entries" entries/reducer}))

(def empty-store
  {"entries" entries/initial-state
   :navigation navigation/initial-state})

(defn- setup []
  (create-store reducer :initial-state empty-store))

(defn ^:export main! []
  (debug/set-logging-enabled!)
  (setup)
  (js/console.debug "[main] started"))

(defn ^:export init []
  (js/console.debug "[main] init done"))

(defn ^:dev/after-load reload! []
  (debug/clear-log)
  (js/console.debug "[main] reloaded"))
