(ns strohm-native.flow
  (:require [strohm-native.impl.flow :as impl]
            [strohm-native.log :as log]
            [strohm-native.tx :refer [send-props!]]
            [strohm-native.utils :as utils])
  (:require-macros [strohm-native.flow]))

(defonce ^:export store (atom nil))

(defn ^:export create-store
  [& args]
  (reset! store (apply impl/create-store args)))

(defn ^:export get-state
  []
  (:state @store))

(defn ^:export dispatch!
  [action]
  (log/debug "dispatch!" action)
  (swap! store (:dispatch @store) action)
  action)

(def ^:export dispatch impl/dispatch)

(defn ^:export dispatch-from-native
  [serialized-action]
  (log/debug "dispatch-from-native" serialized-action)
  (try
    (let [action (utils/js->clj' (js/JSON.parse serialized-action))]
      (dispatch! action))
    (catch ExceptionInfo ex-info
      (tap> {:ex-info ex-info})
      (throw (js/Error. (ex-message ex-info))))
    (catch js/Error js-error
      (tap> {:js-error js-error})
      (throw js-error))
    (catch :default e
      (tap> e)
      (throw e))))

(defn- subscribe-and-send-current-value
  [key watch-fn]
  (add-watch store key watch-fn)
  (watch-fn key nil nil @store))

(defn ^:export subscribe!
  [callback]
  (let [key (random-uuid)
        watch-fn (fn [_key _ref old new]
                   (log/debug "Triggered cljs subscription" key)
                   (callback (:state old) (:state new)))]
    (subscribe-and-send-current-value key watch-fn)
    key))

(defn- trigger-subscription-update-to-native
  [props-spec key _ref old new]
  (log/debug "Triggered native subscription" key)
  (let [old-props (impl/state->props (:state old) props-spec)
        new-props (impl/state->props (:state new) props-spec)]
    (send-props! key old-props new-props)))

(defn ^:export subscribe-from-native
  [subscription-id serialized-props-spec]
  (try
    (let [props-spec (utils/js->clj' (js/JSON.parse serialized-props-spec))
          watch-fn   (partial trigger-subscription-update-to-native props-spec)]
      (log/debug "subscribe-from-native" subscription-id props-spec)
      (subscribe-and-send-current-value (uuid subscription-id) watch-fn)
      subscription-id)
    (catch ExceptionInfo ex-info
      (tap> {:ex-info ex-info})
      (throw (js/Error. (ex-message ex-info))))
    (catch js/Error js-error
      (tap> {:js-error js-error})
      (throw js-error))
    (catch :default e
      (tap> e)
      (throw e))))

(defn ^:export unsubscribe!
  [key]
  (remove-watch store key))

(defn ^:export unsubscribe-from-native
  [subscription-id]
  (log/debug "unsubscribe-from-native" subscription-id)
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
