(ns strohm.native-test
  (:require [cljs.test :refer [deftest is testing]]
            [strohm.native :refer [store create-store get-state
                                   dispatch! subscribe! unsubscribe!
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
      (dispatch! {:type :increment})
      (is (= true @subscription-triggered))))

  (testing "basic subscriptions - unsubscribe"
    (reset! store nil)
    (create-store {:increment (fn incfn [state _] (inc state))}
                  :initial-state 0)
    (let [subscription-triggered (atom false)
          subscription-id (subscribe! #(reset! subscription-triggered true))]
      (unsubscribe! subscription-id)
      (dispatch! {:type :increment})
      (is (= false @subscription-triggered))))

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
      (is (= :something (reducer :something {:type "unknown"}))))))
