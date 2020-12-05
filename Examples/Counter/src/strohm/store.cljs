(ns strohm.store
  (:require [strohm.impl.store :as impl]))

(defn create-store
  [& args]
  (reset! impl/global-store (impl/create-store args)))
