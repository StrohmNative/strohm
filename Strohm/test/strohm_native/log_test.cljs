(ns strohm-native.log-test
  (:require [cljs.test :refer [deftest is testing]]
            [strohm-native.log :as log]
            [test-utils :refer-macros [capturing-logs]]))

(deftest log-test
  (testing "debug level is off by default"
    (capturing-logs
     [logged]
     (log/debug "foo debug")
     (is (= [] @logged))))

  (testing "there is an info level"
    (capturing-logs
     [logged]
     (log/info "foo info")
     (is (= [[:info "foo info"]] @logged))))

  (testing "there is a warn level"
    (capturing-logs
     [logged]
     (log/warn "foo warn")
     (is (= [[:warn "foo warn"]] @logged))))

  (testing "there is an error level"
    (capturing-logs
     [logged]
     (log/error "foo error")
     (is (= [[:error "foo error"]] @logged)))))
