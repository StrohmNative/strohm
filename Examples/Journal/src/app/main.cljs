(ns app.main
  (:require [strohm.native :refer [create-store]]))

(defn reducer
  [state _action]
  state)

(defn ^:export main! []
  (create-store reducer :initial-state {})
  (js/console.debug "[main] started"))

(defn ^:export init []
  (js/console.debug "[main] init done"))

(defn ^:dev/after-load reload! []
  (js/console.debug "[main] reloaded"))
