(ns app.entries.reducer-test
  (:require [cljs.test :refer [deftest testing is]]
            [app.entries.reducer :as sut]))

(declare sample-entry-1 sample-entry-2)

(defn make-keyed [entry-list]
  (into {} (map (fn [entry] [(:entry/id entry) entry]) entry-list)))

(deftest reducer-test
  (testing "initial state"
    (is (= 4 (count (vals sut/initial-state)))))
  
  (testing "add entry"
    (let [reduction-1 (sut/reducer {} {:type "add-entry"
                                       :payload sample-entry-1})
          reduction-2 (sut/reducer reduction-1 {:type "add-entry"
                                                :payload sample-entry-2})]
      (is (= (make-keyed [sample-entry-1]) reduction-1))
      (is (= (make-keyed [sample-entry-1 sample-entry-2]) reduction-2))))
  
  (testing "update entry when only matching one present"
    (is (= (make-keyed [(assoc sample-entry-1 :entry/title "updated title")])
           (sut/reducer (make-keyed [sample-entry-1])
                        {:type "update-entry"
                         :payload {:entry/id 1
                                   :entry/title "updated title"}}))))
  
  (testing "update entry with multiple present"
    (is (= (make-keyed [(assoc sample-entry-1 :entry/title "updated title") sample-entry-2])
           (sut/reducer (make-keyed [sample-entry-1 sample-entry-2])
                        {:type "update-entry"
                         :payload {:entry/id 1
                                   :entry/title "updated title"}}))))
  
  (testing "update entry when no matching present"
    (is (= (make-keyed [sample-entry-1 sample-entry-2])
           (sut/reducer (make-keyed [sample-entry-1 sample-entry-2])
                        {:type "update-entry"
                         :payload {:entry/id 3
                                   :entry/title "updated title"}}))))
  
  (testing "remove entry"
    (is (= (make-keyed [sample-entry-2])
           (sut/reducer (make-keyed [sample-entry-1 sample-entry-2])
                        {:type "remove-entry"
                         :payload {:entry/id 1}})))))

(def sample-entry-1 {:entry/id 1
                     :entry/title "My first journal entry"
                     :entry/text "This is my journal entry"
                     :entry/created (.getTime (js/Date.))})
(def sample-entry-2 {:entry/id 2
                     :entry/title "Another journal entry"
                     :entry/text "I got loads more to tell"
                     :entry/created (.getTime (js/Date.))})
