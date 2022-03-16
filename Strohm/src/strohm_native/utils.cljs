(ns strohm-native.utils)

(defn namespaced-name
  [kwd]
  (str (when-let [kwd-ns (namespace kwd)]
         (str kwd-ns "/"))
       (name kwd)))

(defn clj->js'
  "A version of clj->js that keeps namespaces when keys are keywords."
  [x]
  (clj->js x :keyword-fn namespaced-name))

(defn js->clj'
  "A version of js->clj that keeps namespaces when keys are keywords."
  [x]
  (js->clj x :keywordize-keys true))
