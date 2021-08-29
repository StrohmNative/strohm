(ns strohm-native.impl.log)

(def ^{:dynamic true} *cur-log-level* :info)
(def ^{:dynamic true} *log-fn* tap>)

(defn log-level [] *cur-log-level*)

(def ^:private valid-log-levels 
  ;; Ordered from most verbose to least verbose
  [:debug :info :warn :error])

(defn set-log-level! [new-level]
  (when ((set valid-log-levels) new-level)
    (set! *cur-log-level* new-level)))

(defn log [[level :as args]]
  (let [levels (set (drop-while (partial not= *cur-log-level*) valid-log-levels))]
    (when (levels level)
      (*log-fn* args))))
