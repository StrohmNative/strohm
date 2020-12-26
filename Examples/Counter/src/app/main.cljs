(ns app.main
  (:require [strohm.store :refer [create-store]]
            [strohm.tx :refer [js->native]]
            [strohm.debug :refer [log]]
            [clojure.string :as str]))

(defn ^:export main! []
  (create-store (fn [_action state] state))
  (println "[main] started"))

(defn sendTestMessage []
  (let [time ((juxt #(.getHours %) #(.getMinutes %) #(.getSeconds %)) (js/Date.))]
    (log "send test message"
         (str/join ":" time))
    (js->native {:foo :bar
                 :when time})))

(defn ^:export init []
  (js/setTimeout sendTestMessage 3000)
  (log "init done"))

(defn ^:dev/after-load reload! []
  (println "[main] reloaded"))
