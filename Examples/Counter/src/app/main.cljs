(ns app.main
  (:require [strohm.store :refer [create-store]]
            [strohm.tx :refer [js->swift]]
            [strohm.debug :refer [log]]
            [clojure.string :as str]))

(defn main! []
  (create-store (fn [_action state] state))
  (println "[main] started"))

(defn sendTestMessage []
  (let [time ((juxt #(.getHours %) #(.getMinutes %) #(.getSeconds %)) (js/Date.))]
    (log "send test message"
         (str/join ":" time))
    (js->swift {:foo :bar 
                :when time})))

(defn init []
  (js/setTimeout sendTestMessage 3000)
  (log "init done"))

(defn ^:dev/after-load reload! []
  (println "[main] reloaded"))
