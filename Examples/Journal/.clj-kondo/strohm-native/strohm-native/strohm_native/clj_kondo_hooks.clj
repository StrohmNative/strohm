(ns strohm-native.clj-kondo-hooks
  (:require [clj-kondo.hooks-api :as api]))

(def =>? #{'=> :ret})
(def |? #{'| :st})
(def known-sym? #{'=> '| '<-})

(defn args+gspec+body
  [nodes]
  (let [;; argv      (first nodes)
        gspec     (first nodes)
        body      (next nodes)
        gspec'    (->> gspec
                       (:children)
                       (filterv #(-> % :value known-sym? not))
                       (api/vector-node))
        implicit-args (api/vector-node [(api/token-node '_state) (api/token-node '_action)])
        new-nodes (list* implicit-args gspec' body)]
    ;; gspec: [arg-specs* (| arg-preds+)? => ret-spec (| fn-preds+)? (<- generator-fn)?]
    (if (not= 1 (count (filter =>? (api/sexpr gspec))))
      (api/reg-finding! (merge (meta gspec)
                               {:message (str "Gspec requires exactly one `=>` or `:ret`")
                                :type    :clj-kondo.fulcro.>defn/invalid-gspec}))
      (let [p (partition-by (comp not =>? api/sexpr) (:children gspec))
            [arg [=>] [ret-spec & _output]] (if (-> p ffirst api/sexpr =>?)
                                              (cons [] p) ;; arg-specs might be empty
                                              p)
            [arg-specs [| & arg-preds]] (split-with (comp not |? api/sexpr) arg)]

        (when-not ret-spec
          (api/reg-finding! (merge (meta =>)
                                   {:message "Missing return spec."
                                    :type    :clj-kondo.fulcro.>defn/invalid-gspec})))

        ;; (| arg-preds+)?
        (when (and | (empty? arg-preds))
          (api/reg-finding! (merge (meta |)
                                   {:message "Missing argument predicates after |."
                                    :type    :clj-kondo.fulcro.>defn/invalid-gspec})))


        (let [arg-difference (- (count arg-specs) 2)]
          (if (not (zero? arg-difference))
            (let [too-many-specs? (pos? arg-difference)]
              (api/reg-finding! (merge
                                 (meta (if too-many-specs?
                                         (nth arg-specs (+ 2 arg-difference -1)) ;; first excess spec
                                         gspec))
                                 {:message (str "Reducers have two arguments. "
                                                "Too " (if too-many-specs? "many" "few") " specs.")
                                  :type :clj-kondo.strohm-native.>defnreducer/invalid-gspec})))
            (when (not= (api/sexpr ret-spec) (api/sexpr (first arg-specs)))
              (api/reg-finding! (merge
                                 (meta ret-spec)
                                 {:message "A reducer's first spec should equal its return spec."
                                  :type :clj-kondo.strohm-native.>defnreducer/return-spec-not-equal-to-first-arg-spec})))))
        (let [reducer-body (first body)]
          (if-not (api/map-node? reducer-body)
            (api/reg-finding! (merge
                               (meta reducer-body)
                               {:message "Reducer body should be a map."
                                :type :clj-kondo.strohm-native.>defnreducer/invalid-body}))
            (let [reducer-map (partition 2 (:children reducer-body))]
              (doseq [wrong-key-finding
                      (->> reducer-map
                           (filter (fn [[key-node value-node]]
                                     (not (or (api/keyword-node? key-node)
                                              (api/string-node? key-node)))))
                           (map (fn [[key-node _]]
                                  (merge
                                   (meta key-node)
                                   {:message "Reducer map keys should be strings or keywords."
                                    :type :clj-kondo.strohm-native.>defnreducer/invalid-body}))))]
                (api/reg-finding! wrong-key-finding)))))))
    new-nodes))

(defn >defnreducer
  [{:keys [node]}]
  (let [args       (rest (:children node))
        fn-name    (first args)
        ?docstring (when (some-> (second args) api/sexpr string?)
                     (second args))
        args       (if ?docstring
                     (nnext args)
                     (next args))
        post-docs  (args+gspec+body args)
        post-name  (if ?docstring
                     (list* ?docstring post-docs)
                     post-docs)
        new-node   (api/list-node
                    (list*
                     (api/token-node 'defn)
                     fn-name
                     post-name))]
    {:node new-node}))

(defn args+body
  [nodes finding-type]
  (let [body      nodes
        implicit-args (api/vector-node [(api/token-node '_state) (api/token-node '_action)])
        new-nodes (list* implicit-args body)]
    (let [reducer-body (first body)]
      (if-not (api/map-node? reducer-body)
        (api/reg-finding! (merge
                           (meta reducer-body)
                           {:message "Reducer body should be a map."
                            :type finding-type}))
        (let [reducer-map (partition 2 (:children reducer-body))]
          (doseq [wrong-key-finding
                  (->> reducer-map
                       (filter (fn [[key-node value-node]]
                                 (not (or (api/keyword-node? key-node)
                                          (api/string-node? key-node)))))
                       (map (fn [[key-node _]]
                              (merge
                               (meta key-node)
                               {:message "Reducer map keys should be strings or keywords."
                                :type finding-type}))))]
            (api/reg-finding! wrong-key-finding)))))
    new-nodes))

(defn defnreducer
  [{:keys [node]}]
  (let [args       (rest (:children node))
        fn-name    (first args)
        ?docstring (when (some-> (second args) api/sexpr string?)
                     (second args))
        args       (if ?docstring
                     (nnext args)
                     (next args))
        post-docs  (args+body args :clj-kondo.strohm-native.defnreducer/invalid-body)
        post-name  (if ?docstring
                     (list* ?docstring post-docs)
                     post-docs)
        new-node   (api/list-node
                    (list*
                     (api/token-node 'defn)
                     fn-name
                     post-name))]
    {:node new-node}))

(defn defreducer
  [{:keys [node]}]
  (let [args       (rest (:children node))
        ?docstring (when (some-> (first args) api/sexpr string?)
                     (first args))
        args       (if ?docstring
                     (next args)
                     args)
        post-docs  (args+body args :clj-kondo.strohm-native.defreducer/invalid-body)
        post-name  (if ?docstring
                     (list* ?docstring post-docs)
                     post-docs)
        new-node   (api/list-node
                    (list*
                     (api/token-node 'fn)
                     post-name))]
    {:node new-node}))
