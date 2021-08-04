(ns test-utils)

(defmacro capturing-logs [[atom-name] & body]
  `(let [~atom-name (atom [])]
     (binding [strohm.impl.log/*log-fn* (partial swap! ~atom-name conj)]
       ~@body)))
