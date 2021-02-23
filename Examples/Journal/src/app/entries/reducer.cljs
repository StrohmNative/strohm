(ns app.entries.reducer
  (:require [strohm.native :refer [create-reducer]]))

(defn update-entry [payload entry]
  (if (= (:entry/id payload) (:entry/id entry))
    (merge entry payload)
    entry))

(def reducer
  (create-reducer {"add-entry" conj
                   "update-entry" #(map (partial update-entry %2) %1)
                   "remove-entry" #(filter (fn [entry] (not= %2 (:entry/id entry))) %1)}))

(def initial-state [{:entry/id 1
                     :entry/title "Title 1"
                     :entry/text "Text 1"
                     :entry/created (double (- (.getTime (js/Date.)) 60000))}
                    {:entry/id 2
                     :entry/title "Title 2"
                     :entry/text "Text 2"
                     :entry/created (double (- (.getTime (js/Date.)) 10000))}
                    {:entry/id 3
                     :entry/title "Title 3"
                     :entry/text "Text 3"
                     :entry/created (double (.getTime (js/Date.)))}
                    {:entry/id 4
                     :entry/title "Lorem Ipsum"
                     :entry/text "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
                     :entry/created (double (.getTime (js/Date.)))}])
