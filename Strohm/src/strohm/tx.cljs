(ns strohm.tx
  (:require [strohm.utils :refer [clj->js']]))

(defn- android-webview? 
  []
  (.call (.-hasOwnProperty (.-prototype js/Object)) js/globalThis "strohmReceiveProps"))

(defn- ios-javascriptcore? 
  []
  (.call (.-hasOwnProperty (.-prototype js/Object)) js/globalThis "postMessage"))

(defn props->message [subscriptionId old-props new-props]
  {:function "subscriptionUpdate"
   :subscriptionId (str subscriptionId)
   :old old-props
   :new new-props})

(defn send-message! [message]
  (cond
    (android-webview?)
    (.receiveProps (.-strohmReceiveProps js/globalThis)
                   (js/JSON.stringify (clj->js' message)))

    (ios-javascriptcore?)
    ((.-postMessage js/globalThis) (clj->js' message))

    :else
    (js/console.error "Neither Android nor iOS callback interface was found.")))

(defn send-props!
  [subscriptionId old-props new-props]
  (send-message! (props->message subscriptionId old-props new-props)))
