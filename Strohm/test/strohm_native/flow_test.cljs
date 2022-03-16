(ns strohm-native.flow-test
  (:require [cljs.test :refer [deftest is testing]]
            [strohm-native.flow :refer [store create-store get-state
                                        dispatch! dispatch subscribe! unsubscribe!
                                        create-reducer]]))

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
    (is (= 1 (get-state))))

  (testing "basic subscriptions"
    (reset! store nil)
    (create-store {:increment (fn incfn [state _] (inc state))}
                  :initial-state 0)
    (let [subscription-triggered (atom false)]
      (subscribe! #(reset! subscription-triggered true))
      (is (= true @subscription-triggered))))

  (testing "subscribe calls callback with current value"
    (reset! store nil)
    (create-store {} :initial-state :foo)
    (let [received-old-state (atom nil)
          received-new-state (atom nil)]
      (subscribe! (fn [old new]
                    (reset! received-old-state old)
                    (reset! received-new-state new)))
      (is (nil? @received-old-state))
      (is (= :foo @received-new-state))))

  (testing "basic subscriptions - unsubscribe"
    (reset! store nil)
    (create-store {:increment (fn incfn [state _] (inc state))}
                  :initial-state 0)
    (let [did-dispatch (atom false)
          subscription-id (subscribe! (fn [old _] (when old (reset! did-dispatch true))))]
      (unsubscribe! subscription-id)
      (dispatch! {:type :increment})
      (is (= false @did-dispatch))))

  (testing "basic subscriptions - old and new state"
    (reset! store nil)
    (create-store {:increment (fn incfn [state _] (inc state))}
                  :initial-state 0)
    (let [received-old-state (atom nil)
          received-new-state (atom nil)]
      (subscribe! (fn [old new]
                    (reset! received-old-state old)
                    (reset! received-new-state new)))
      (dispatch! {:type :increment})
      (is (= 0 @received-old-state))
      (is (= 1 @received-new-state))))

  (testing "create reducer"
    (let [reducer       (create-reducer {"append" conj "plus" +})
          append-action {:type "append" :payload :foo}
          plus-action   {:type "plus"   :payload 2}]
      (is (= [:foo] (reducer [] append-action)))
      (is (= 3 (reducer 1 plus-action)))
      (is (= :something (reducer :something {:type "unknown"})))))

  (testing "middleware dispatches extra action"
    (let [reducer        (create-reducer {:extra #(assoc %1 :extra true)})
          dispatch-extra (fn [next]
                           (fn [store action]
                             (cond-> store
                               (= (:type action) :test) (dispatch {:type :extra})
                               true                     (next action))))]
      (reset! store nil)
      (create-store reducer :middlewares [dispatch-extra])
      (dispatch! {:type :test})
      (is (true? (:extra (get-state)))))))
