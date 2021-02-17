(ns strohm.impl.store-test
  (:require [cljs.test :refer [deftest testing is]]
            [clojure.string :as str]
            [strohm.impl.store :refer [create-store
                                       reduce-action
                                       combine-reducers]]))

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
    (let [reducer {:increment #(inc %1)
                   :decrement #(dec %1)
                   :add +}
          store   (create-store reducer :initial-state 0)]
      (is (=  1 (:state (reduce-action {:type :increment}      store))))
      (is (= -1 (:state (reduce-action {:type :decrement}      store))))
      (is (=  0 (:state (reduce-action {:type :unknown}        store))))
      (is (=  3 (:state (reduce-action {:type :add :payload 3} store))))))

  (testing "nested reducer functions"
    (let [sub1-reducer (fn sub1-reducer [state action]
                         (if (= :test (:type action))
                           (str/join "-1-" [state (:payload action)])
                           state))
          sub2-reducer (fn sub2-reducer [state action]
                         (if (= :test (:type action))
                           (str/join "-2-" [state (:payload action)])
                           state))
          root-reducer (fn root-reducer [state action]
                         {:sub1 (sub1-reducer (:sub1 state) action)
                          :sub2 (sub2-reducer (:sub2 state) action)})
          store        (create-store root-reducer {:sub1 "" :sub2 ""})]
      (is (= {:sub1 "-1-test" :sub2 "-2-test"}
             (:state (reduce-action {:type :test :payload "test"} store))))))
  
  (testing "combine-reducers with reducer functions"
    (let [sub1-reducer (fn sub1-reducer [state action]
                         (if (= "test" (:type action))
                           (str/join "-1-" [state (:payload action)])
                           state))
          sub2-reducer (fn sub2-reducer [state action]
                         (if (= "test" (:type action))
                           (str/join "-2-" [state (:payload action)])
                           state))
          root-reducer (combine-reducers {:sub1 sub1-reducer :sub2 sub2-reducer})
          store        (create-store root-reducer {:sub1 "" :sub2 ""})]
      (is (= {:sub1 "-1-test" :sub2 "-2-test"}
             (:state (reduce-action {:type "test" :payload "test"} store))))))
  
  (testing "combine-reducers with reducer maps"
    (let [sub1-reducer {:test #(str/join "-1-" [%1 %2])}
          sub2-reducer {:test #(str/join "-2-" [%1 %2])}
          root-reducer (combine-reducers {:sub1 sub1-reducer :sub2 sub2-reducer})
          store        (create-store root-reducer {:sub1 "" :sub2 ""})]
      (is (= {:sub1 "-1-test" :sub2 "-2-test"}
             (:state (reduce-action {:type :test :payload "test"} store)))))))
