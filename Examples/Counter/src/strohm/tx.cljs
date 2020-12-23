(ns strohm.tx)

(defn js->swift [msg]
  (let [handler  (->> js/window
                      (.-webkit)
                      (.-messageHandlers)
                      (.-jsToSwift))]
    (.postMessage handler (clj->js msg))))
