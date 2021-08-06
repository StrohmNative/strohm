(ns app.main
  (:require [strohm.native :refer [create-store combine-reducers]]
            [app.entries.reducer :as entries]
            [app.navigation.reducer :as navigation]
            [strohm.tx :as tx]
            [strohm.log :as log]
            [cljs.reader :refer [read-string]]))

(def reducer (combine-reducers {"entries" entries/reducer}))

(defn send-state! [state]
  (let [serialized-state (pr-str state)]
    (tx/send-message! {:function "persistState"
                       :state serialized-state})))

(defn persist-state-middleware
  [next]
  (fn [store action]
    (let [next-store (next store action)]
      (send-state! (:state next-store))
      next-store)))

(defn load-state []
  (some-> (.-strohmPersistedState js/globalThis)
          read-string))

(def empty-store
  {"entries"   entries/initial-state
   :navigation navigation/initial-state})

(defn- setup []
  (let [initial-state (or (load-state) empty-store)]
    (create-store reducer
                  :initial-state initial-state
                  :middlewares [persist-state-middleware])))

(defn ^:export main! []
  (log/set-log-level! :debug)
  (setup)
  (log/debug "[main] started"))

(defn ^:export init []
  (log/debug "[main] init done"))

(defn ^:dev/after-load reload! []
  (log/debug "[main] reloaded"))
