(ns app.main
  (:require [strohm.store :refer [create-store]]))

(defn main! []
  (create-store (fn [_action state] state))
  (println "[main] started"))

(defn ^:dev/after-load reload! []
  (println "[main] reloaded"))
