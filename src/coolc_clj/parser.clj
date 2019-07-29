(ns cooljc-clj.parser
  (:require [clj-antlr.core :as antlr]))

(def cool-parser
  (antlr/parser "resources/cool.g4"))

(defn parse
  [input]
  (antlr/parse cool-parser input))

(def examples
  ["C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/complex.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/arith.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/atoi.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/atoi_test.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/book_list.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/cells.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/complex.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/cool.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/graph.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/hairyscary.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/hello_world.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/io.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/lam.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/life.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/list.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/new_complex.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/palindrome.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/primes.cl"
   "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/sort_list.cl"])


