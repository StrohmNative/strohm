(ns strohm-native.clj-kondo-hooks-test
  (:require [clj-kondo.core :as clj-kondo]
            [clojure.test :refer [deftest testing is]]))

(defn clj-kondo-findings
  [input-string]
  ;; ensure latest hook is used
  (require '[clj-kondo.impl.hooks] :reload)
  (-> input-string
      (with-in-str
        (clj-kondo/run!
         {:lint  ["-"]
          :cache false
          :config
          '{:hooks {:analyze-call {strohm-native.flow/defnreducer
                                   strohm-native.clj-kondo-hooks/defnreducer
                                   strohm-native.flow/>defnreducer
                                   strohm-native.clj-kondo-hooks/>defnreducer}}
            :linters {:clj-kondo.strohm-native.>defnreducer/invalid-gspec {:level :error}
                      :clj-kondo.strohm-native.>defnreducer/return-spec-not-equal-to-first-arg-spec {:level :error}
                      :clj-kondo.strohm-native.>defnreducer/invalid-body {:level :error}
                      :clj-kondo.strohm-native.defnreducer/invalid-body {:level :error}}}}))
      (:findings)))

(deftest >defnreducer-happy-path
  (is (= []
         (clj-kondo-findings
          "(ns foo
             (:require [clojure.spec.alpha :as s]
                       [strohm-native.flow :refer [>defnreducer]]))
           (>defnreducer
             sample-reducer
             [(s/keys) (s/tuple keyword? any?) => (s/keys)]
             {\"sample-action\" #(assoc %1 :sample %2)})"))))

(deftest defnreducer-happy-path
  (is (= []
         (clj-kondo-findings
          "(ns foo
             (:require [strohm-native.flow :refer [defnreducer]]))
           (defnreducer
             sample-reducer
             {\"sample-action\" #(assoc %1 :sample %2)})"))))

(deftest >defnreducer-invalid-gspec
  (is (= [{:type :clj-kondo.strohm-native.>defnreducer/invalid-gspec
           :message "Reducers have two arguments. Too few specs."
           :col 14 :end-col 36
           :row 6 :end-row 6
           :filename "<stdin>"
           :level :error}]
         (clj-kondo-findings
          "(ns foo
             (:require [clojure.spec.alpha :as s]
                       [strohm-native.flow :refer [>defnreducer]]))
           (>defnreducer
             sample-reducer
             [(s/keys) => (s/keys)]
             {\"sample-action\" #(assoc %1 :sample %2)})")))
  (is (= [{:type :clj-kondo.strohm-native.>defnreducer/invalid-gspec
           :message "Reducers have two arguments. Too many specs."
           :col 48 :end-col 52
           :row 6 :end-row 6
           :filename "<stdin>"
           :level :error}]
         (clj-kondo-findings
          "(ns foo
             (:require [clojure.spec.alpha :as s]
                       [strohm-native.flow :refer [>defnreducer]]))
           (>defnreducer
             sample-reducer
             [(s/keys) (s/tuple keyword? any?) any? => (s/keys)]
             {\"sample-action\" #(assoc %1 :sample %2)})"))))

(deftest >defnreducer-invalid-body
  (testing "body is not a map"
    (is (= [{:type :clj-kondo.strohm-native.>defnreducer/invalid-body
             :message "Reducer body should be a map."
             :col 16 :end-col 20
             :row 7 :end-row 7
             :filename "<stdin>"
             :level :error}]
           (clj-kondo-findings
            "(ns foo
               (:require [clojure.spec.alpha :as s]
                         [strohm-native.flow :refer [>defnreducer]]))
             (>defnreducer
               sample-reducer
               [(s/keys) (s/tuple keyword? any?) => (s/keys)]
               :foo)"))))

  (testing "body is not a map"
    (is (= [{:type :clj-kondo.strohm-native.>defnreducer/invalid-body
             :message "Reducer body should be a map."
             :col 16 :end-col 20
             :row 7 :end-row 7
             :filename "<stdin>"
             :level :error}]
           (clj-kondo-findings
            "(ns foo
               (:require [clojure.spec.alpha :as s]
                         [strohm-native.flow :refer [>defnreducer]]))
             (>defnreducer
               sample-reducer
               [(s/keys) (s/tuple keyword? any?) => (s/keys)]
               :foo)")))

    (is (= [{:type :clj-kondo.strohm-native.>defnreducer/invalid-body
             :message "Reducer map keys should be strings or keywords."
             :col 17 :end-col 18
             :row 7 :end-row 7
             :filename "<stdin>"
             :level :error}
            {:type :clj-kondo.strohm-native.>defnreducer/invalid-body
             :message "Reducer map keys should be strings or keywords."
             :col 17 :end-col 18
             :row 8 :end-row 8
             :filename "<stdin>"
             :level :error}]
           (clj-kondo-findings
            "(ns foo
               (:require [clojure.spec.alpha :as s]
                         [strohm-native.flow :refer [>defnreducer]]))
             (>defnreducer
               sample-reducer
               [(s/keys) (s/tuple keyword? any?) => (s/keys)]
               {1 #(assoc %1 :foo %2)
                2 #(assoc %1 :foo %2)})")))))

(deftest defnreducer-invalid-body
  (testing "body is not a map"
    (is (= [{:type :clj-kondo.strohm-native.defnreducer/invalid-body
             :message "Reducer body should be a map."
             :col 16 :end-col 20
             :row 5 :end-row 5
             :filename "<stdin>"
             :level :error}]
           (clj-kondo-findings
            "(ns foo
               (:require [strohm-native.flow :refer [defnreducer]]))
             (defnreducer
               sample-reducer
               :foo)"))))

  (testing "body is not a map"
    (is (= [{:type :clj-kondo.strohm-native.defnreducer/invalid-body
             :message "Reducer body should be a map."
             :col 16 :end-col 20
             :row 5 :end-row 5
             :filename "<stdin>"
             :level :error}]
           (clj-kondo-findings
            "(ns foo
               (:require [strohm-native.flow :refer [defnreducer]]))
             (defnreducer
               sample-reducer
               :foo)")))

    (is (= [{:type :clj-kondo.strohm-native.defnreducer/invalid-body
             :message "Reducer map keys should be strings or keywords."
             :col 17 :end-col 18
             :row 5 :end-row 5
             :filename "<stdin>"
             :level :error}
            {:type :clj-kondo.strohm-native.defnreducer/invalid-body
             :message "Reducer map keys should be strings or keywords."
             :col 17 :end-col 18
             :row 6 :end-row 6
             :filename "<stdin>"
             :level :error}]
           (clj-kondo-findings
            "(ns foo
               (:require [strohm-native.flow :refer [defnreducer]]))
             (defnreducer
               sample-reducer
               {1 #(assoc %1 :foo %2)
                2 #(assoc %1 :foo %2)})")))))
