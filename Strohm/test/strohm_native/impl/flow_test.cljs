(ns strohm-native.impl.flow-test
  (:require [cljs.test :refer [deftest testing is]]
            [clojure.string :as str]
            [strohm-native.flow :refer-macros [defreducer*]]
            [strohm-native.impl.flow :refer [create-store
                                             combine-reducers
                                             state->props
                                             dispatch]]))

(defn identity-reducer
  [state _action]
  state)

(deftest store-impl-test
  (testing "a store has a reducer, state and a dispatch function"
    (let [store (create-store identity-reducer)]
      (is (= {} (:state store)))
      (is (= identity-reducer (:reducer store)))))

  (testing "a store can be created with an initial state"
    (is (= {:test "foo"}
           (:state (create-store identity-reducer
                                 :initial-state {:test "foo"})))))

  (testing "it can reduce an action to a new state"
    (let [reducer (fn incfn [state _] (inc state))
          store   (create-store reducer :initial-state 0)]
      (is (= 1 (:state (dispatch store {:type :increment}))))))

  (testing "a reducer can be a map"
    (let [reducer {:increment #(inc %1)
                   :decrement #(dec %1)
                   :add +}
          store   (create-store reducer :initial-state 0)]
      (is (=  1 (:state (dispatch store {:type :increment}))))
      (is (= -1 (:state (dispatch store {:type :decrement}))))
      (is (=  0 (:state (dispatch store {:type :unknown}))))
      (is (=  3 (:state (dispatch store {:type :add :payload 3}))))))

  (testing "nested reducer functions"
    (let [sub1-reducer (fn sub1-reducer
                         [state action]
                         (if (= :test (:type action))
                           (str/join "-1-" [state (:payload action)])
                           state))
          sub2-reducer (fn sub2-reducer
                         [state action]
                         (if (= :test (:type action))
                           (str/join "-2-" [state (:payload action)])
                           state))
          root-reducer (fn root-reducer
                         [state action]
                         {:sub1 (sub1-reducer (:sub1 state) action)
                          :sub2 (sub2-reducer (:sub2 state) action)})
          store        (create-store root-reducer :initial-state {:sub1 "" :sub2 ""})]
      (is (= {:sub1 "-1-test" :sub2 "-2-test"}
             (:state (dispatch store {:type :test :payload "test"}))))))

  (testing "combine-reducers with reducer functions"
    (let [sub1-reducer (fn sub1-reducer
                         [state action]
                         (if (= "test" (:type action))
                           (str/join "-1-" [state (:payload action)])
                           state))
          sub2-reducer (fn sub2-reducer
                         [state action]
                         (if (= "test" (:type action))
                           (str/join "-2-" [state (:payload action)])
                           state))
          root-reducer (combine-reducers {:sub1 sub1-reducer :sub2 sub2-reducer})
          store        (create-store root-reducer :initial-state {:sub1 "" :sub2 ""})]
      (is (= {:sub1 "-1-test" :sub2 "-2-test"}
             (:state (dispatch store {:type "test" :payload "test"}))))))

  (testing "combine-reducers with reducer maps"
    (let [sub1-reducer (defreducer* {"test" #(str/join "-1-" [%1 %2])})
          sub2-reducer {"test" #(str/join "-2-" [%1 %2])}
          root-reducer (combine-reducers {"sub1" sub1-reducer :sub2 sub2-reducer}) ;; TODO: moet vectors worden
          store        (create-store root-reducer :initial-state {"sub1" "" :sub2 ""})]
      (is (= {"sub1" "-1-test" :sub2 "-2-test"}
             (:state (dispatch store {:type "test" :payload "test"}))))))

  (testing "combine-reducers with unknown action"
    (let [sub-reducer  (defreducer* {"test" #(str/join "-1-" [%1 %2])})
          root-reducer (combine-reducers {"sub" sub-reducer})
          store        (create-store root-reducer :initial-state {"sub" "foo"})]
      (is (= {"sub" "foo"} (:state (dispatch store {:type "unknown-action-type"}))))))

  (testing "combine-reducers with reducer maps and unknown action"
    (let [sub-reducer  {"test" #(str/join "-1-" [%1 %2])}
          root-reducer (combine-reducers {"sub" sub-reducer})
          store        (create-store root-reducer :initial-state {"sub" "foo"})]
      (is (= {"sub" "foo"} (:state (dispatch store {:type "unknown-action-type"}))))))

  (testing "state->props"
    (is (= ["my-prop" {:foo :bar}]
           (state->props {:foo :bar} ["my-prop" []])))
    (is (= ["my-prop" :bar]
           (state->props {:foo :bar} ["my-prop" [:foo]])))
    (is (= ["my-prop" :bar]
           (state->props {"foo" :bar} ["my-prop" ["foo"]])))
    (is (= ["my-prop" {:goal "target"}]
           (state->props {:foo :bar
                          :baz [0 1 {:goal "target"}]}
                         ["my-prop" [:baz 2]]))))

  (testing "state->props accepts strings in path"
    (is (= ["my-prop" :bar]
           (state->props {:test-ns/foo :bar} ["my-prop" ["test-ns/foo"]]))))

  (testing "state->props accepts strings with colons in path"
    (is (= ["my-prop" :bar]
           (state->props {:test-ns/foo :bar} ["my-prop" [":test-ns/foo"]])))))

(deftest middleware-test
  (testing "middleware dispatches extra action"
    (let [dispatch-extra (fn [next]
                           (fn [store action]
                             (cond-> store
                               (= (:type action) :test) (dispatch {:type :extra})
                               true                     (next action))))
          store (create-store {:extra #(assoc %1 :extra true)}
                              :middlewares [dispatch-extra])]
      (is (true? (-> store
                     (dispatch {:type :test})
                     :state
                     :extra)))))

  (testing "multiple middlewares"
    (let [plus-10-middleware (fn [next]
                               (fn [store action]
                                 (next store (update action :payload + 10))))
          times-2-middleware (fn [next]
                               (fn [store action]
                                 (next store (update action :payload * 2))))
          minus-4-middleware (fn [next]
                               (fn [store action]
                                 (next store (update action :payload - 4))))
          store (create-store {:test (fn [state payload]
                                       (assoc state :value payload))}
                              :initial-state {:value nil}
                              :middlewares [plus-10-middleware
                                            times-2-middleware
                                            minus-4-middleware])]
      (is (= (- (* (+ 10 1) 2) 4) (-> store
                                      (dispatch {:type :test :payload 1})
                                      :state
                                      :value))))))
