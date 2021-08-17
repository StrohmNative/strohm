(ns strohm.log
  (:require [strohm.impl.log :as impl]
            [strohm.utils :as utils]))

(defn set-log-level! [new-level]
  (impl/set-log-level! new-level))

(defn debug [& args]
  (impl/log (cons :debug args)))

(defn info [& args]
  (impl/log (cons :info args)))

(defn warn [& args]
  (impl/log (cons :warn args)))

(defn error [& args]
  (impl/log (cons :error args)))

(defn ^:export log-from-native [serialized-args]
  (-> serialized-args
      js/JSON.parse
      utils/js->clj'
      (update 0 keyword)
      impl/log))
