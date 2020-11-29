(ns strohm.impl.store-test
  (:require [cljs.test :refer [deftest testing is]]
            [strohm.impl.store :refer [create-store state reducer dispatcher]]))

(defn identity-reducer [state _action] state)

(deftest store-impl-test
  (testing "a store has a reducer, state and a dispatch function"
    (let [store (create-store 'some-reducer)]
      (is (= {} (state store)))
      (is (= 'some-reducer (reducer store)))
      (is (fn? (dispatcher store)))))

  (testing "a store can be created with an initial state"
    (is (= {:test "foo"}
           (state (create-store 'some-reducer
                                :initial-state {:test "foo"}))))))
