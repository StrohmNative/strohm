(ns strohm-native.spec
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check]
            [clojure.test.check.generators]
            [clojure.test.check.properties]
            [com.fulcrologic.guardrails.core :refer [>def]]))

(>def :strohm/prop-name (s/or :keyword keyword?
                              :string string?))

(>def :strohm/state any?)

(>def :strohm/prop-path
  (s/coll-of (s/or :keyword keyword?
                   :index nat-int?
                   :string string?)
             :kind vector?))

(>def :strohm/prop-spec (s/tuple :strohm/prop-name :strohm/prop-path))
(>def :strohm/prop-value (s/tuple :strohm/prop-name any?))
(>def :strohm/reducer-fn (s/with-gen ifn? #(s/gen #{(fn [state _action] state)})))
(>def :strohm/reducer (s/or :fn :strohm/reducer-fn :map map?))
(>def :strohm/dispatch (s/with-gen ifn? #(s/gen #{(fn [store _action] store)})))
(>def :strohm/store (s/keys :opt-un [:strohm/state :strohm/reducer :strohm/dispatch]))
(>def ::store-action-fn :strohm/dispatch)

(>def :strohm/middleware
  (s/fspec :args (s/cat :next :strohm/dispatch)
           :ret ::store-action-fn))

(>def :strohm/middlewares (s/coll-of :strohm/middleware))
(>def :strohm/action (s/with-gen any? #(s/gen #{{:type :sample-action}})))
(>def :strohm/subscription-key uuid?)
