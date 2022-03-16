(ns strohm-native.impl.flow
  (:require [com.fulcrologic.guardrails.core :refer [>defn >defn- ?]]
            [clojure.spec.alpha :as s]
            [strohm-native.spec]))

(def default-initial-state {})

(>defn- identity-reducer
  [state _]
  [:strohm/state :strohm/action => :strohm/state]
  state)

(>defn compute-reducer-fn
  [reducer action-type]
  [:strohm/reducer any? => :strohm/reducer-fn]
  (if (associative? reducer)
    (if-let [reducer-for-action (get reducer action-type)]
      (fn reducer-from-map [state action]
        (reducer-for-action state (:payload action)))
      identity-reducer)
    reducer))

(>defn- reduce-action
  [store action]
  [:strohm/store :strohm/action => :strohm/store]
  (let [reducer     (:reducer store)
        reducing-fn (compute-reducer-fn reducer (or (:type action) nil))]
    (update store
            :state
            (fn reduce-action-update-store [state] (reducing-fn state action)))))

(>defn- apply-substate-reducer
  [action state substate-key reducer]
  [:strohm/action :strohm/state any? :strohm/reducer => :strohm/state]
  (let [reducer-fn (compute-reducer-fn reducer (or (:type action) nil))]
    (update state
            substate-key
            (fn [substate] (reducer-fn substate action)))))

(>defn combine-reducers
  [reducers]
  [map? => :strohm/reducer]
  (fn combined-reducer [state action]
    (reduce-kv (partial apply-substate-reducer action)
               state
               reducers)))

(>defn state-for-prop-spec
  [state [prop-name prop-path]]
  [:strohm/state :strohm/prop-spec => :strohm/prop-value]
  [prop-name (reduce (fn [acc prop] (get acc prop)) state prop-path)])

(>defn state->props
  [state prop-spec]
  [:strohm/state :strohm/prop-spec => :strohm/prop-value]
  (state-for-prop-spec state prop-spec))


(>defn create-store'
  [reducer initial-state middlewares]
  [:strohm/reducer (? any?) (? :strohm/middlewares) => :strohm/store]
  {:state (or initial-state default-initial-state)
   :reducer reducer
   :dispatch (if (seq middlewares)
               ((apply comp middlewares) reduce-action)
               reduce-action)})

(>defn create-store
  [reducer & {:keys [initial-state middlewares]}]
  [:strohm/reducer (s/* any?) => :strohm/store]
  (create-store' reducer initial-state middlewares))

(>defn dispatch
  [store action]
  [:strohm/store :strohm/action => :strohm/store]
  ((:dispatch store) store action))
