(ns strohm-native.impl.flow)

(def default-initial-state {})

(defn- identity-reducer [state _] state)

(defn compute-reducer-fn [reducer action-type]
  (if (associative? reducer)
    (if-let [reducer-for-action (get reducer action-type)]
      (fn reducer-from-map [state action]
        (reducer-for-action state (:payload action)))
      identity-reducer)
    reducer))

(defn- reduce-action [store action]
  (let [reducer     (:reducer store)
        reducing-fn (compute-reducer-fn reducer (:type action))]
    (update store
            :state
            (fn [state] (reducing-fn state action)))))

(defn- apply-substate-reducer
  [action state substate-key reducer]
  (let [reducer-fn (compute-reducer-fn reducer (:type action))]
    (update state
            substate-key
            (fn [substate] (reducer-fn substate action)))))

(defn combine-reducers [reducers]
  (fn combined-reducer [state action]
    (reduce-kv (partial apply-substate-reducer action)
               state
               reducers)))

(defn state-for-prop-spec [state [prop-name prop-spec]]
  [prop-name
   (reduce (fn [acc prop] (get acc prop)) state prop-spec)])

(defn state->props [state props-spec]
  (into {} (map (partial state-for-prop-spec state) props-spec)))

(defn create-store
  [reducer & {:keys [initial-state middlewares]}]
  {:state (or initial-state default-initial-state)
   :reducer reducer
   :dispatch (if (seq middlewares)
               ((apply comp (reverse middlewares)) reduce-action)
               reduce-action)})

(defn dispatch [store action]
  ((:dispatch store) store action))
