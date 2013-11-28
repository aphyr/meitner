# Meitner

<img src="https://raw.github.com/aphyr/meitner/master/doc/meitner.jpg"
style="width: 100%" />

## Usage

Meitner infers the static dependency graph of Clojure functions, macros, and
other vars through syntactic analysis of their source. Meitner uses Riddley for
macroexpansion and analysis of lexical scope. Relies on
`clojure.repl/source-fn`, so I'm not sure how to use it outside the repl right
now.

## Try me

```clj
user=> (require ['meitner.core :refer '[deps unfold]])
nil
```

Here's a function:

```clj
user=> (source second)
(def
 ^{:doc "Same as (first (next x))"
   :arglists '([x])
   :added "1.0"
   :static true}
 second (fn ^:static second [x] (first (next x))))
nil
```

What's it depend on?

```clj
user=> (deps 'second)
#{clojure.core/next clojure.core/first}
```

How about a recursive function?

```clj
user=> (source last)
(def 
 ^{:arglists '([coll])
   :doc "Return the last item in coll, in linear time"
   :added "1.0"
   :static true}
 last (fn ^:static last [s]
        (if (next s)
          (recur (next s))
          (first s))))
user=> (deps 'last)
#{clojure.core/next clojure.core/last clojure.core/first}
```

What about the full dependency tree, pruning cycles?

```clj
user=> (pprint (unfold deps 'partition))
{partition
 {clojure.core/cons {},
  clojure.core/doall
  {clojure.core/dorun
   {clojure.core/next {},
    clojure.core/dorun :recursive,
    clojure.core/seq {}}},
  clojure.core/nthrest
  {clojure.core/nthrest :recursive,
   clojure.core/seq {},
   clojure.core/rest {}},
  clojure.core/list {},
  clojure.core/concat
  {clojure.core/cons {},
   clojure.core/first {},
   clojure.core/chunk-rest {},
   clojure.core/chunk-cons {},
   clojure.core/next {},
   clojure.core/chunked-seq? {clojure.core/instance? {}},
   clojure.core/chunk-first {},
   clojure.core/seq {},
   clojure.core/rest {}},
  clojure.core/count {},
  clojure.core/seq {},
  clojure.core/take
  {clojure.core/cons {},
   clojure.core/first {},
   clojure.core/seq {},
   clojure.core/rest {}}}}
nil
```

So `partition` depends on `cons`, `doall` (which in turn depends on `dorun`,
which depends on `next`, `seq`, and `dorun` recursively), and so on.

## License

Copyright © 2013 Kyle Kingsbury

Distributed under the Eclipse Public License, the same as Clojure.
