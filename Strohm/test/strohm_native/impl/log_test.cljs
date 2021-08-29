(ns strohm-native.impl.log-test
  (:require [cljs.test :refer [deftest is testing]]
            [strohm-native.impl.log :as log]
            [test-utils :refer-macros [capturing-logs]]))

(deftest log-impl-test
  (testing "calls a provided log function"
    (capturing-logs
     [logged]
     (log/log [:error "message"])
     (is (= [[:error "message"]] @logged))))

  (testing "it taps by default"
    (capturing-logs [logged]
                    (log/log [:error "foo"])
                    (is (= [[:error "foo"]] @logged))))

  (testing "default log level is :info"
    (is (= :info (log/log-level))))

  (testing "log level can be changed"
    (let [old-log-level (log/log-level)]
      (log/set-log-level! :error)
      (is (= :error (log/log-level)))
      (log/set-log-level! old-log-level)))

  (testing "it ignores an invalid log level"
    (let [old-log-level (log/log-level)]
      (log/set-log-level! :invalid-log-level)
      (is (= old-log-level (log/log-level)))))

  (testing "it treats log level as minimum level"
    (testing "level :debug"
      (binding [log/*cur-log-level* :debug]
        (capturing-logs
         [logged]
         (log/log [:error "e"])
         (log/log [:warn "w"])
         (log/log [:info "i"])
         (log/log [:debug "d"])
         (is (= ["e" "w" "i" "d"] (mapcat second @logged))))))

    (testing "level :info"
      (binding [log/*cur-log-level* :info]
        (capturing-logs
         [logged]
         (log/log [:error "e"])
         (log/log [:warn "w"])
         (log/log [:info "i"])
         (log/log [:debug "d"])
         (is (= ["e" "w" "i"] (mapcat second @logged))))))
    
    (testing "level :warn"
      (binding [log/*cur-log-level* :warn]
        (capturing-logs
         [logged]
         (log/log [:error "e"])
         (log/log [:warn "w"])
         (log/log [:info "i"])
         (log/log [:debug "d"])
         (is (= ["e" "w"] (mapcat second @logged))))))
    
    (testing "level :error"
      (binding [log/*cur-log-level* :error]
        (capturing-logs
         [logged]
         (log/log [:error "e"])
         (log/log [:warn "w"])
         (log/log [:info "i"])
         (log/log [:debug "d"])
         (is (= ["e"] (mapcat second @logged))))))))
