(ns strohm-native.utils-test
  (:require [cljs.test :refer [deftest is testing]]
            [strohm-native.utils :refer [namespaced-name clj->js' js->clj']]))

(deftest utils-test
  (testing "namespaced-name"
    (is (= "foo" (namespaced-name :foo)))
    (is (= "foo/bar" (namespaced-name :foo/bar))))
  
  (testing "clj->js' and js->clj'"
    (is (= {:foo/bar "bar/baz"}
           (js->clj' (clj->js' {:foo/bar :bar/baz}))))))
