(ns strohm.store-test
  (:require [cljs.test :refer [deftest is testing]]
            [strohm.store :refer [store create-store get-state dispatch!]]))

(deftest store-test
  (testing "store is nil initially"
    (is (nil? @store)))

  (testing "created store has initial state"
    (reset! store nil)
    (create-store {} :initial-state {:foo "bar"})
    (is (= {:foo "bar"} (get-state))))
  
  (testing "dispatch an action"
    (reset! store nil)
    (create-store {:increment (fn incfn [state _] (inc state))}
                  :initial-state 0)
    (is (= {:type :increment} (dispatch! {:type :increment})))
    (is (= 1 (get-state)))))
