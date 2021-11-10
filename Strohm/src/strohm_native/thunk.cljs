(ns strohm-native.thunk)

(defn thunk-middleware [next]
    (fn [store action]
      (if (fn? action)
        (action store #((:state store)))
        (next store action))))
