(ns strohm-native.flow
  (:require [com.fulcrologic.guardrails.core :refer [>defn*]]))

(defn generate-reducer-fn
  [reducer action-type]
  (if (associative? reducer)
    (if-let [reducer-for-action (get reducer action-type)]
      `(fn [state# action#]
         (~reducer-for-action state# (:payload action#)))
      `(fn identity-reducer# [state# action#] state#))
    `~reducer))

(defmacro defreducer
  [reducer-name reducer-map]
  (let [all-reducers (into {} (map (fn [[action-type _]]
                                     [action-type
                                      (generate-reducer-fn reducer-map action-type)])
                                   reducer-map))]
    `(defn ~reducer-name [state# action#]
       (if-let [reducer-for-action# (get ~all-reducers (:type action#))]
         (reducer-for-action# state# action#)
         state#))))

(defmacro >defreducer
  [reducer-name gspec reducer-map]
  (let [all-reducers (into {} (map (fn [[action-type _]]
                                     [action-type
                                      (generate-reducer-fn reducer-map action-type)])
                                   reducer-map))]
    (>defn*
     &env &form
     `(~reducer-name
       [state# action#]
       ~gspec
       (if-let [reducer-for-action# (get ~all-reducers (:type action#))]
         (reducer-for-action# state# action#)
         state#))
     {:private? false})))
