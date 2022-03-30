(ns strohm-native.thunk-test
  (:require [cljs.test :refer [deftest testing is]]
            [strohm-native.flow :refer [store create-store! get-state
                                        dispatch! dispatch create-reducer]]
            [strohm-native.thunk :refer [thunk-middleware]]))

(deftest thunk-middleware-test
  (testing "use thunk to dispatch extra action"
    (let [reducer (create-reducer {:extra #(update %1 :extra inc)})]
      (reset! store nil)
      (create-store! reducer :middlewares [thunk-middleware] :initial-state {:extra 0})
      (dispatch! (fn [store _get-state]
                   (-> store
                       (dispatch {:type :extra})
                       (dispatch {:type :extra})
                       (dispatch {:type :extra}))))
      (is (= 3 (:extra (get-state)))))))
