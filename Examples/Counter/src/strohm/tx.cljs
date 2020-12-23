(ns strohm.tx)

(defn- js->swift-handler []
  (.. js/window
      -webkit
      -messageHandlers
      -jsToSwift))

(defn js->native
  [msg]
  {:pre [(associative? msg)]}
  (.postMessage (js->swift-handler) (clj->js msg)))
