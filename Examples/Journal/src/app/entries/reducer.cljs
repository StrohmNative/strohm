(ns app.entries.reducer
  (:require [strohm.native :refer [create-reducer]]))

(def initial-state [])

(defn update-entry [payload entry]
  (if (= (:entry/id payload) (:entry/id entry))
    (merge entry payload)
    entry))

(def reducer
  (create-reducer {"add-entry" conj
                   "update-entry" #(map (partial update-entry %2) %1)
                   "remove-entry" #(filter (fn [entry] (not= %2 (:entry/id entry))) %1)}))
