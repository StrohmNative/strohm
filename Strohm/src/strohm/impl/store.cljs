(ns strohm.impl.store)

(def default-initial-state {})

(defn create-store 
  [reducer & {:keys [initial-state]}]
  {:state (or initial-state default-initial-state)
   :reducer reducer})

(defn- identity-reducer [state _] state)

(defn reducer-map->function [reducer action]
  (if (associative? reducer)
    (or ((:type action) reducer) identity-reducer)
    reducer))

(defn reduce-action [action store] 
  (let [reducer     (:reducer store)
        reducing-fn (reducer-map->function reducer action)]
    (update store
            :state
            (fn [state] (reducing-fn state action)))))

(defn combine-reducers [reducers]
  (fn combined-reducer [state action]
    (into {} (for [[state-key reducer] reducers
                   :let [reducer-fn (reducer-map->function reducer action)]]
               [state-key (reducer-fn (get state state-key) action)]))))
