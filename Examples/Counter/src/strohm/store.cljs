(ns strohm.store
  (:require [clojure.string :as str]
            [strohm.debug :as debug]
            [strohm.tx :refer [send-props]]
            [strohm.impl.store :as impl]))

(defonce ^:export store (atom nil))

(defn create-store
  [& args]
  (reset! store (apply impl/create-store args)))

(defn get-state
  []
  (:state @store))

(defn ^:export dispatch!
  [action]
  (debug/log "dispatch!" action)
  (swap! store (partial impl/reduce-action action))
  action)

(defn ^:export dispatch-from-native
  [serialized-action]
  (debug/log "dispatch-from-native" serialized-action)
  (let [action (js->clj (js/JSON.parse serialized-action) :keywordize-keys true)] 
    (dispatch! action)))

(defn subscribe!
  [callback]
  (let [key (random-uuid)]
    (add-watch store key (fn [_key _ref old new] 
                           (debug/log "Triggered cljs subscription" _key)
                           (callback (:state old) (:state new))))
    key))

(defn- trigger-subscription-update-to-native
  [props-spec k _ref old new] 
  (debug/log "Triggered native subscription" k)
  (let [old-props (into {} (map (fn [[prop-name _prop-spec]] [prop-name (:state old)]) props-spec))
        new-props (into {} (map (fn [[prop-name _prop-spec]] [prop-name (:state new)]) props-spec))]
    (send-props k old-props new-props)))

(defn ^:export subscribe-from-native
  [subscription-id serialized-props-spec]
  (let [key        (uuid subscription-id)
        props-spec (js->clj (js/JSON.parse serialized-props-spec))]
    (debug/log "subscribe-from-native" subscription-id props-spec)
    (add-watch store key (partial trigger-subscription-update-to-native props-spec))
    subscription-id))

(defn unsubscribe!
  [key]
  (remove-watch store key))

(defn ^:export unsubscribe-from-native
  [subscription-id]
  (unsubscribe! subscription-id))
