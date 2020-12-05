(ns strohm.store
  (:require [strohm.impl.store :as impl]))

(defonce store (atom nil))

(defn create-store
  [& args]
  (reset! store (apply impl/create-store args)))

(defn get-state
  []
  (:state @store))

(defn dispatch!
  [action]
  (swap! store (partial impl/reduce-action action))
  action)
