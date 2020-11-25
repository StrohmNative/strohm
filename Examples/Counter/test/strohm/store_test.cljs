(ns strohm.store-test
  (:require [cljs.test :refer [deftest is testing]]
            [strohm.store :refer [store state]]))

(deftest store-test 
  (testing "there is a store"
    (is (some? store)))
  
  (testing "it has state"
    (is (= {} (state)))))
