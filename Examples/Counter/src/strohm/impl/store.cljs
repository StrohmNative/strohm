(ns strohm.impl.store)

(def default-initial-state {})

(defn create-store 
  [reducer & {:keys [initial-state]}]
  {:state (or initial-state default-initial-state)
   :reducer reducer})

(defn- identity-reducer [state _] state)

(defn reduce-action [action store] 
  (let [reducer     (:reducer store)
        reducing-fn (if (associative? reducer)
                      (or ((:type action) reducer) identity-reducer)
                      reducer)]
    (update store
            :state
            (fn [state] (reducing-fn state action)))))
