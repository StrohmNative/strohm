(ns app.main
  (:require [strohm.native :refer [create-store]]
            [app.entries.reducer :as entries]
            [app.navigation.reducer :as navigation]))

(defn reducer
  [state _action]
  state)

(def empty-store
  {:entries entries/initial-state
   :navigation navigation/initial-state})

(defn ^:export main! []
  (create-store reducer :initial-state empty-store)
  (js/console.debug "[main] started"))

(defn ^:export init []
  (js/console.debug "[main] init done"))

(defn ^:dev/after-load reload! []
  (js/console.debug "[main] reloaded"))
