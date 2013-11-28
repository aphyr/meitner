(ns meitner.core-test
  (:require [clojure.test :refer :all]
            [meitner.core :refer :all]))

(deftest source-expr-test
  (is (= (source-expr 'source-expr)
         '())))
