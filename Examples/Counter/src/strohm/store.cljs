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

(defn subscribe!
  [callback]
  (let [key (random-uuid)]
    (add-watch store key (fn [_key _ref _old-val _new-val] (callback)))
    key))

(defn unsubscribe!
  [key]
  (remove-watch store key))
