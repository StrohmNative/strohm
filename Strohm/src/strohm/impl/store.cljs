(ns strohm.impl.store)

(def default-initial-state {})

(defn create-store 
  [reducer & {:keys [initial-state]}]
  {:state (or initial-state default-initial-state)
   :reducer reducer})

(defn- identity-reducer [state _] state)

(defn- get-reducer-fn [reducer action]
  (if (associative? reducer)
    (or ((:type action) reducer)
        identity-reducer)
    reducer))

(defn reduce-action [action store] 
  (let [reducer     (:reducer store)
        reducing-fn (get-reducer-fn reducer action)]
    (update store
            :state
            (fn [state] (reducing-fn state action)))))

(defn combine-reducers [reducers]
  (fn combined-reducer [state action]
    (reduce-kv (fn [next-state key reducer]
                 (let [reducer-fn (get-reducer-fn reducer action)]
                   (update next-state
                           key
                           #(reducer-fn (get % key) action))))
               state
               reducers)))
