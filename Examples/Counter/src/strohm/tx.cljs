(ns strohm.tx)

(defn js->native [msg]
  (let [handler  (->> js/window
                      (.-webkit)
                      (.-messageHandlers)
                      (.-jsToSwift))]
    (.postMessage handler (clj->js msg))))
