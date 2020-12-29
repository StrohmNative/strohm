(ns strohm.debug
  (:require [clojure.string :as str]))

(defn log [& args]
  (if-let [document js/document]
    (let [contentDiv (.getElementById document "content")
          oldHtml    (.-innerHTML contentDiv)]
      (set! (.-innerHTML contentDiv)
            (str oldHtml "<br/>\n[js] " (str/join " " args))))
    (apply js/console.debug args)))

(defn clear-log []
  (let [contentDiv (.getElementById js/document "content")]
    (set! (.-innerHTML contentDiv) "")))
(comment (clear-log))
