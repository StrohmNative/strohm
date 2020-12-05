(ns strohm.impl.store)

(def default-initial-state {})

(defn state
  [store]
  (:state store))

(defn reducer 
  [store]
  (:reducer store))

(defn dispatcher
  [store]
  (:dispatcher store))

(defn create-store 
  [reducer & {:keys [initial-state]}]
  {:state (or initial-state default-initial-state)
   :reducer reducer
   :dispatcher identity})

(defn reduce-action [store action] 
  (update store :state (partial (:reducer store) action)))
