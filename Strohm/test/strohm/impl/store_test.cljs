(ns strohm.impl.store-test
  (:require [cljs.test :refer [deftest testing is]]
            [strohm.impl.store :refer [create-store
                                       reduce-action]]))

(defn identity-reducer [state _action] state)

(deftest store-impl-test
  (testing "a store has a reducer, state and a dispatch function"
    (let [store (create-store 'some-reducer)]
      (is (= {} (:state store)))
      (is (= 'some-reducer (:reducer store)))))

  (testing "a store can be created with an initial state"
    (is (= {:test "foo"}
           (:state (create-store 'some-reducer
                                 :initial-state {:test "foo"})))))

  (testing "it can reduce an action to a new state"
    (let [reducer (fn incfn [state _] (inc state))
          store   (create-store reducer :initial-state 0)]
      (is (= 1 (:state (reduce-action {:type :increment} store))))))
  
  (testing "a reducer can be a map"
    (let [reducer {:increment (fn incfn [state _] (inc state))
                   :decrement (fn decfn [state _] (dec state))}
          store   (create-store reducer :initial-state 0)]
      (is (= 1  (:state (reduce-action {:type :increment} store))))
      (is (= -1 (:state (reduce-action {:type :decrement} store))))
      (is (= 0  (:state (reduce-action {:type :unknown}   store)))))))
