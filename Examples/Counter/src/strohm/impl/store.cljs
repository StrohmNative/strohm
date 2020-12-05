(ns strohm.impl.store)

(def default-initial-state {})

(defonce global-store (atom nil))

(defn create-store 
  [reducer & {:keys [initial-state]}]
  {:state (or initial-state default-initial-state)
   :reducer reducer})

(defn reduce-action [action store] 
  (update store :state (partial (:reducer store) action)))
