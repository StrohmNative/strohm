(ns strohm.native
  (:require [strohm.debug :as debug]
            [strohm.tx :refer [send-props!]]
            [strohm.utils :as utils]
            [strohm.impl.store :as impl]))

(defonce ^:export store (atom nil))

(defn ^:export create-store
  [& args]
  (reset! store (apply impl/create-store args)))

(defn ^:export get-state
  []
  (:state @store))

(defn ^:export dispatch!
  [action]
  (debug/log "dispatch!" action)
  (swap! store (:dispatch @store) action)
  action)

(def ^:export dispatch impl/dispatch)

(defn ^:export dispatch-from-native
  [serialized-action]
  (debug/log "dispatch-from-native" serialized-action)
  (let [action (utils/js->clj' (js/JSON.parse serialized-action))]
    (dispatch! action)))

(defn- subscribe-and-send-current-value [key watch-fn]
  (add-watch store key watch-fn)
  (watch-fn key nil nil @store))

(defn ^:export subscribe!
  [callback]
  (let [key (random-uuid)
        watch-fn (fn [_key _ref old new]
                   (debug/log "Triggered cljs subscription" key)
                   (callback (:state old) (:state new)))]
    (subscribe-and-send-current-value key watch-fn)
    key))

(defn- trigger-subscription-update-to-native
  [props-spec key _ref old new]
  (debug/log "Triggered native subscription" key)
  (let [old-props (impl/state->props (:state old) props-spec)
        new-props (impl/state->props (:state new) props-spec)]
    (send-props! key old-props new-props)))

(defn ^:export subscribe-from-native
  [subscription-id serialized-props-spec]
  (let [props-spec (utils/js->clj' (js/JSON.parse serialized-props-spec))
        watch-fn   (partial trigger-subscription-update-to-native props-spec)]
    (debug/log "subscribe-from-native" subscription-id props-spec)
    (subscribe-and-send-current-value (uuid subscription-id) watch-fn)
    subscription-id))

(defn ^:export unsubscribe!
  [key]
  (remove-watch store key))

(defn ^:export unsubscribe-from-native
  [subscription-id]
  (debug/log "unsubscribe-from-native" subscription-id)
  (let [key (uuid subscription-id)]
    (some? (unsubscribe! key))))

(def ^:export combine-reducers impl/combine-reducers)

(defn ^:export create-reducer 
  [reducer-map]
  (let [all-reducers (into {} (map (fn [[action-type _]]
                                     [action-type
                                      (impl/compute-reducer-fn reducer-map action-type)])
                                   reducer-map))]
    (fn [state action]
      (if-let [reducer-for-action (get all-reducers (:type action))]
        (reducer-for-action state action)
        state))))

(def ^:export clj->js' utils/clj->js')
(def ^:export js->clj' utils/js->clj')
