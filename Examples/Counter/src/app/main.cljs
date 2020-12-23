(ns app.main
  (:require [strohm.store :refer [create-store]]
            [clojure.string :as str]))

(defn main! []
  (create-store (fn [_action state] state))
  (println "[main] started"))

(defn log [& args]
  (let [contentDiv (.getElementById js/document "content")
        oldHtml    (.-innerHTML contentDiv)]
    (set! (.-innerHTML contentDiv) 
          (str oldHtml "<br/>\n[js] " (str/join " " args)))))

(defn clear-log []
  (let [contentDiv (.getElementById js/document "content")]
    (set! (.-innerHTML contentDiv) "")))
(comment (clear-log))

(defn js->swift [msg]
  (let [handler  (->> js/window
                      (.-webkit)
                      (.-messageHandlers)
                      (.-jsToSwift))]
    (.postMessage handler msg)))

(defn sendTestMessage []
  (log "send test message"
       (str/join ":" ((juxt #(.getHours %) #(.getMinutes %) #(.getSeconds %)) (js/Date.))))
  (js->swift "{\"foo\":\"bar\"}"))

(defn init []
  (js/setTimeout sendTestMessage 3000)
  (log "init done"))

(defn ^:dev/after-load reload! []
  (println "[main] reloaded"))
