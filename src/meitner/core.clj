(ns meitner.core
  (:require [clojure.repl :refer [source-fn]]
            [riddley.walk :refer [walk-exprs]]
            [riddley.compiler :refer [locals]]))

(defn var->ns
  "Takes a var and returns the namespace."
  [^clojure.lang.Var v]
  (when v
    (.ns v)))

(defn var->symbol
  "Takes a var and returns its fully qualified symbol name."
  [^clojure.lang.Var v]
  (when v
    (symbol (str (.ns v)) (str (.sym v)))))

(defn fully-qualified
  "Returns a fully qualified symbol."
  [sym]
  (-> sym resolve var->symbol))

(defn symbol->ns
  "Converts symbols to namespaces."
  [sym]
  (-> sym resolve var->ns))

(defn source-expr
  "Returns the source expression of a function, by symbol."
  [fun]
  (when-let [text (source-fn (fully-qualified fun))]
    (read-string text)))

(defn fn-bodies
  "Returns the bodies of a function expression."
  [fn-expr]
  (if (some vector? fn-expr)
    ; There's a single function body.
    (-> fn-expr
        (drop-while (complement vector?))
        rest
        (list))
    ; Multiple function bodies.
    (-> fn-expr
        (drop-while (complement list?))
        (map rest))))

(defn foo
  [x]
  (recur (+ x)))

(defn deps
  "Takes a symbol pointing to a function and returns a set of the symbols it
  depends on."
  [fun]
  (let [deps (atom #{})]
    (binding [*ns* (var->ns (resolve fun))]
      (walk-exprs symbol?
                  (fn [sym]
                    (cond
                      ; Recursion
                      (= 'recur sym)
                      (swap! deps conj (fully-qualified fun))

                      ; Skip locals
                      (contains? (locals) sym)
                      false

                      ; Skip classes
                      (class? (resolve sym))
                      false

                      :else
                      (when-let [full (fully-qualified sym)]
                        (swap! deps conj full)))
                    sym)
                  (source-expr fun))
      @deps)))

(defn unfold
  "Takes a function which, given a node in a graph, returns its neighbors, and
  an initial node. Returns a map of that node's neighbors to *their* neighbors
  to *their* neighbors and so on, pruning circular deps."
  ([graph initial]
   {initial (unfold graph initial #{})})
  ([graph node visited]
   (let [visited (conj visited node)]
     (reduce (fn [m neighbor]
               (assoc m neighbor
                      (if (visited neighbor)
                        :recursive
                        (unfold graph neighbor visited))))
             (array-map)
             (graph node)))))
