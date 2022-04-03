(ns app.entries.reducer
  (:require [strohm-native.flow :refer-macros [defreducer]]))

(defn- update-entry
  [entries payload]
  (if (get entries (:entry/id payload))
    (update entries (:entry/id payload) (fn [entry] (merge entry payload)))
    entries))

(defreducer reducer
  {"new-entry" #(let [id (str (random-uuid))]
                  (assoc %1 id {:entry/id id
                                :entry/title "Untitled"
                                :entry/text ""
                                :entry/created (double (.getTime (js/Date.)))}))
   "add-entry" #(assoc %1 (:entry/id %2) %2)
   "update-entry" update-entry
   "remove-entry" #(dissoc %1 (:entry/id %2))})

(def initial-state
  (into {} (map (fn [entry] [(:entry/id entry) entry])
                [{:entry/id (str (random-uuid))
                  :entry/title "Title 1"
                  :entry/text "Text 1"
                  :entry/created (double (- (.getTime (js/Date.)) (* 15 24 3600 1000)))}
                 {:entry/id (str (random-uuid))
                  :entry/title "Title 2"
                  :entry/text "Text 2"
                  :entry/created (double (- (.getTime (js/Date.)) (* 5 24 3600 1000)))}
                 {:entry/id (str (random-uuid))
                  :entry/title "Title 3"
                  :entry/text "Text 3"
                  :entry/created (double (- (.getTime (js/Date.)) (* 2 24 3600 1000)))}
                 {:entry/id (str (random-uuid))
                  :entry/title "Lorem Ipsum"
                  :entry/text "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                  :entry/created (double (.getTime (js/Date.)))}])))
