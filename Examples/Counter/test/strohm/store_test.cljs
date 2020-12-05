(ns strohm.store-test
  (:require [cljs.test :refer [deftest is testing]]
            [strohm.store :refer [store create-store get-state 
                                  dispatch! subscribe! unsubscribe!]]))

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
      (is (= false @subscription-triggered)))))
