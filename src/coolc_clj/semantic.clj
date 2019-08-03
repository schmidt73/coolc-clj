(ns coolc-clj.semantic
  (:require [coolc-clj.parser :as parser]
            [clojure.walk :as walk]))

(def default-class "Object")
(def base-classes #{"IO" "Object" "String" "Int" "Bool"})

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
     :attributes (mapv second (filter is-attribute? seq-forms))
     :methods (mapv second (filter is-method? seq-forms))}))

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
            (= tag :classDefine) (parse-class form))
          form))
      parse-tree)))

(defn find-class
  [classes name]
  (first (filter #(= name (:name %)) classes)))

(defn inheritance-valid?
  "Returns true if the class inherits from a class
   defined in classes or a base class, nil otherwise"
  [classes class]
  (or
    (contains? base-classes (:inherits class))
    (not (nil? (find-class classes (:inherits class))))))

(defn contains-cycle?
  "Returns true if a node in the inheritance graph
   contains a cycle."
  [classes class]
  (loop [current (find-class classes (:inherits class))
         seen #{(:name class)}]
    (cond
      (nil? current) false
      (contains? seen (:name current)) true
      :otherwise (recur
                   (find-class classes (:inherits current))
                   (conj seen (:name current))))))

(defn find-cycles
  "Finds all cycles in an inheritance graph."
  [classes]
  (filter #(contains-cycle? classes %) classes))

(defn verify-class-hierarchy
  "Verifies the class hierarchy, throwing an exception if"
  [parse-tree]
  (let [classes (parse-classes parse-tree)
        undefined-inheritances (filter #(not (inheritance-valid? classes %)) classes)
        cycles (find-cycles classes)]
    (when (not (empty? undefined-inheritances))
      (throw (ex-info "Undefined inheritance" {:nodes undefined-inheritances})))
    (when (not (empty? cycles))
      (throw (ex-info "Inheritance hierarchy is cyclic." {:cycles cycles})))))

;;;; EXAMPLES

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

(def io-example (slurp (nth examples 11)))
(def io-tree (parser/parse io-example))

(doseq [example examples]
  (let [parse-tree (parser/parse (slurp example))]
    (verify-class-hierarchy parse-tree)))
