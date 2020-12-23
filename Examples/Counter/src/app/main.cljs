(ns app.main
  (:require [strohm.store :refer [create-store]]))

(defn main! []
  (create-store (fn [_action state] state))
  (println "[main] started"))

(defn log [msg]
  (let [contentDiv (.getElementById js/document "content")
        oldHtml    (.-innerHTML contentDiv)]
    (set! (.-innerHTML contentDiv) 
          (str oldHtml "<br/>\n[js] " msg))))

(defn ^:dev/after-load reload! []
  (println "[main] reloaded"))
