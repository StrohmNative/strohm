(ns app.reducer-test
  (:require [app.main :as sut]
            [cljs.test :refer [deftest testing is]]))

(deftest app-reducer
  (testing "unknown action has no effect"
    (let [state-before {"entries" (group-by :entry/id [{:entry/id 1} {:entry/id 2}])}
          state-after (sut/reducer state-before {:type "unknown"})]
      (is (= state-after state-before))))

  (testing "removing non-existing entry has no effect"
    (let [state-before {"entries" (group-by :entry/id [{:entry/id 1} {:entry/id 2}])}
          state-after (sut/reducer state-before {:type "remove-entry" :payload {:entry/id 3}})]
      (is (= state-after state-before))))

  (testing "remove existing entry"
    (let [state-before {"entries" (group-by :entry/id [{:entry/id 1} {:entry/id 2}])}
          state-after (sut/reducer state-before {:type "remove-entry" :payload {:entry/id 2}})]
      (is (= state-after {"entries" (group-by :entry/id [{:entry/id 1}])})))))
