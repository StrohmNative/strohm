(ns strohm.debug
  (:require [clojure.string :as str]))

(defn log [& args]
  (let [contentDiv (.getElementById js/document "content")
        oldHtml    (.-innerHTML contentDiv)]
    (set! (.-innerHTML contentDiv)
          (str oldHtml "<br/>\n[js] " (str/join " " args)))))

(defn clear-log []
  (let [contentDiv (.getElementById js/document "content")]
    (set! (.-innerHTML contentDiv) "")))
(comment (clear-log))
