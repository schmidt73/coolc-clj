(ns coolc-clj.parser
  (:require [clj-antlr.core :as antlr]))

(def cool-parser
  (antlr/parser "resources/cool.g4"))

(defn parse
  [input]
  (antlr/parse cool-parser input))
