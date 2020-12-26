(ns strohm.store
  (:require [strohm.impl.store :as impl]))

(defonce ^:export store (atom nil))

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
    (add-watch store key (fn [_key _ref old new] 
                           (callback (:state old) (:state new))))
    key))

(defn unsubscribe!
  [key]
  (remove-watch store key))
