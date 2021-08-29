(ns strohm-native.tx
  (:require [strohm-native.utils :refer [clj->js']]))

(defn- js->swift-handler []
  (.. js/window -webkit -messageHandlers -jsToSwift))

(defn- android-webview? 
  []
  (.call (.-hasOwnProperty (.-prototype js/Object)) js/globalThis "strohmReceiveProps"))

(defn- ios-webview?
  []
  (some? (.-webkit js/window)))

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

    (ios-webview?)
    (.postMessage (js->swift-handler)
                  (clj->js' message))

    :else
    (js/console.error "Neither Android nor iOS callback interface was found.")))

(defn send-props!
  [subscriptionId old-props new-props]
  (send-message! (props->message subscriptionId old-props new-props)))
