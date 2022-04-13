(ns strohm-native.flow
  (:require [clojure.spec.alpha :as s]
            [com.fulcrologic.guardrails.core :refer [>defn >def >fdef]]
            [strohm-native.impl.flow :as impl]
            [strohm-native.log :as log]
            [strohm-native.spec]
            [strohm-native.tx :refer [send-props!]]
            [strohm-native.utils :as utils])
  (:require-macros [strohm-native.flow]))

(defonce ^:export store (atom nil))

(defn ^:export create-store!
  [& args]
  (reset! store (apply impl/create-store args)))

(>defn ^:export get-state
  []
  [=> :strohm/state]
  (:state @store))

(>defn ^:export dispatch!
  [action]
  [:strohm/action => :strohm/action]
  (log/debug "dispatch!" action)
  (swap! store (:dispatch @store) action)
  action)

(def ^:export dispatch impl/dispatch)

(>defn ^:export dispatch-from-native
  [serialized-action]
  [string? => :strohm/action]
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

(>defn ^:export subscribe!
  [callback]
  [[any? any? => any?] => :strohm/subscription-key]
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

(>defn ^:export subscribe-from-native
  [subscription-id serialized-props-spec]
  [string? string? => string?]
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

(>defn ^:export unsubscribe!
  [key]
  [:strohm/subscription-key => nil?]
  (remove-watch store key)
  nil)

(>defn ^:export unsubscribe-from-native
  [subscription-id]
  [string? => boolean?]
  (log/debug "unsubscribe-from-native" subscription-id)
  (let [key (uuid subscription-id)]
    (some? (unsubscribe! key))))

(>defn ^:export combine-reducers
  [reducers]
  [map? => :strohm/reducer]
  (impl/combine-reducers reducers))

(def ^:export clj->js' utils/clj->js')
(def ^:export js->clj' utils/js->clj')
