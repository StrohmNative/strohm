(ns strohm.store
  (:require [strohm.impl.store :as impl]))

(defonce global-store (atom nil))

(defn create-store
  [& args]
  (reset! global-store (impl/create-store args)))
