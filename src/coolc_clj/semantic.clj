(ns coolc-clj.semantic
  (:require [coolc-clj.parser :as parser]
            [clojure.walk :as walk]))

(def base-classes #{"IO" "Object" "String" "Int" "Bool"})

(defn get-first
  "Returns the first item in the collection
   satisfying some predicate or nil if not found."
  [coll predicate]
  (loop [current-coll coll]
    (if-let [x (first current-coll)]
      (if (predicate x)
        x
        (recur (rest current-coll))))))

(defn find-class
  "Finds the class with the given name in the parse
   tree. Nil if not found."
  [parse-tree name]
  (get-first parse-tree #(= name (:name %))))

(defn find-parent-class
  "Returns the closest ancestor in the inheritance hierarchy
   or nil if not found."
  [parse-tree class]
  (:inherits (find-class parse-tree class)))

(defn find-class-hierarchy
  "Finds the inheritance hierarchy of the given class from
   the current node to the root."
  [parse-tree class-name]
  (take-while
    (comp not nil?)
    (iterate #(find-parent-class parse-tree %) class-name)))

(defn find-cycles
  "Returns all nodes that create a cycle in the inheritance graph."
  [parse-tree]
  (let [is-cyclic? #(not (apply distinct? %))]
    (->> (map #(find-class-hierarchy parse-tree (:name %)) parse-tree)
      (filter is-cyclic?)
      (map first))))

(defn check-cycles
  "Verifies the inheritance graph of the parse tree does not contain
   cycles."
  [parse-tree]
  (let [cycles (find-cycles parse-tree)]
    (when (not (empty? cycles))
      (throw (ex-info "Inheritance hierarchy is cyclic." {:cycles cycles})))
    true))

(defn valid-class-name?
  "Returns true if the class has a valid name."
  [parse-tree class]
  (or
    (contains? base-classes (:inherits class))
    (not (nil? (find-class parse-tree (:inherits class))))))

(defn check-class-names
  "Verifies all class names are valid. Throws an exception if they are not."
  [parse-tree]
  (let [undefined-class-names (filter #(not (valid-class-name? parse-tree %)) parse-tree)]
    (when (not (empty? undefined-class-names))
      (throw (ex-info "Undefined class names" {:nodes undefined-class-names})))
    true))

(defn find-method
  "Finds the method associated with the class
   name and method name or returns nil."
  [parse-tree class-name method-name]
  (if-let [class (get-first parse-tree #(= (:name %) class-name))]
    (if-let [method (get-first (:methods class) #(= (:name %) method-name))]
      method)))

(defn find-methods-hierarchy
  "Returns the hierarchy of methods associated with
   class and its inheritance hierarchy."
  [parse-tree class-name method-name]
  (let [class-hierarchy (find-class-hierarchy parse-tree class-name)
        methods (->> class-hierarchy
                  (map #(find-method parse-tree % method-name))
                  (filter (comp not nil?)))]
    (zipmap class-hierarchy methods))) 

(defn eq-signatures?
  [method1 method2]
  (let [params1 (:params method1)
        params2 (:params method2)
        eq-params #(= (:type-id %1) (:type-id %2))]
    (when (and
            (= (:type-id method1) (:type-id method2))
            (= (count params1) (count params2)))
      (reduce #(and %1 %2) true (map eq-params params1 params2)))))

(defn eq-signatures
  ([m1 m2] (when (eq-signatures? m1 m2) m1))
  ([m] m))

(defn check-methods-in-class
  "Verifies all method names are valid in a particular class.
   Throws an exception if they are not."
  [parse-tree class-name]
  (let [method-names (map :name (:methods (find-class parse-tree class-name)))
        method-hierarchys (map #(find-methods-hierarchy parse-tree class-name %) method-names)
        bad-methods (filter #(nil? (reduce eq-signatures (vals %))) method-hierarchys)]
    (when (not (empty? bad-methods))
      (throw (ex-info "Methods must have same signature." {:methods bad-methods})))
    true))

(defn check-methods
  "Checks all methods in all classes. Throws an exception if methods
   are invalid."
  [parse-tree]
  (doall (map #(check-methods-in-class parse-tree (:name %)) parse-tree)))

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

(def lam-tree (parser/parse (slurp (nth examples 12))))

(def primes-tree
  (parser/parse
    (slurp "C:/Users/schmidt73/Desktop/School/Compilers/coolc-clj/examples/primes.cl")))

(def hw-tree (parser/parse (slurp (nth examples 10))))

(doseq [example examples]
  (let [parse-tree (parser/parse (slurp example))]))

(doseq [ptree (map (comp parser/parse slurp) examples)]
  (check-methods ptree))
