(ns coolc-clj.parser
  (:require [clj-antlr.core :as antlr]
            [clojure.walk :as walk]))

(def default-class "Object")
(def cool-parser
  (antlr/parser "resources/cool.g4"))

(defn parse-formal
  [formal-form]
  {:name (nth formal-form 1)
   :type-id (nth formal-form 3)})

(defn parse-formals
  [method-form]
  (let [is-formal? #(and (seq? %) (= :formal (first %)))]
    (mapv parse-formal (filter is-formal? method-form))))

(defn parse-method
  "Parses a method form."
  [method-form]
  (let [formals (parse-formals method-form)
        nformals (count formals)
        type-id-index (+ 5 (* 2 nformals) (if (= 0 nformals) 0 -1))]
    {:name (second method-form)
     :params formals
     :body (nth method-form (+ type-id-index 2))
     :type-id (nth method-form type-id-index)}))

(defn parse-attribute
  "Parses an attribute form."
  [attribute-form]
  (merge {:name (second attribute-form) :type-id (nth attribute-form 3)}
    (if-let [v (nth attribute-form 5 nil)]
      {:value v})))

(defn parse-class
  "Parses a class form in the parse tree."
  [class-form]
  (let [is-method? #(= "(" (nth % 2)) 
        is-attribute? #(= ":" (nth % 2))
        seq-forms (filter seq? class-form)]
    {:name (nth class-form 2)
     :inherits (if (= "inherits" (nth class-form 3))
                 (nth class-form 4)
                 default-class)
     :attributes (mapv parse-attribute (filter is-attribute? seq-forms))
     :methods (mapv parse-method (filter is-method? seq-forms))}))

(defn parse-classes
  "Parses all the classes in the parse tree into a
   vector."
  [parse-tree]
  (filterv coll?
    (walk/prewalk
      (fn [form]
        (if-let [tag (and (seq? form) (first form))]
          (cond
            (= tag :program) (vec (rest form))
            (= tag :classDefine) (parse-class form)
            :otherwise form)
          form))
      parse-tree)))

(defn parse
  [input]
  (parse-classes (antlr/parse cool-parser input)))
