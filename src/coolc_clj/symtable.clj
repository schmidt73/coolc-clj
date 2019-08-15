(ns coolc-clj.symtable)

(defn throw-empty
  "Throws an exception with the message that the symbol table is
   empty."
  [msg]
  (throw (ex-info (str "symbol table is empty: " msg) {:msg msg})))

(defn create-symbol-table 
  "Creates a symbol table for use by semantic analyzer."
  []
  (atom '()))

(defn enter-scope
  "Enters a new scope."
  [sym-table]
  (swap! sym-table #(conj % {})))

(defn exit-scope
  "Exits the current scope."
  [sym-table]
  (when (empty? @sym-table) (throw-empty "can't exit scope."))
  (swap! sym-table rest))

(defn add-symbol
  "Adds a symbol to the current scope in the
   symbol table, associating it with info."
  [sym-table symbol info]
  (when (empty? @sym-table) (throw-empty "can't exit scope."))
  (swap! sym-table #(conj (rest %) (assoc (first %) symbol info))))

(defn lookup-symbol
  "Looks up a symbol in the table. Returning the
   associated info or nil if symbol is not found."
  [sym-table symbol]
  (when (empty? @sym-table) (throw-empty "can't lookup symbol."))
  (loop [current @sym-table]
    (when (not (empty? current))
      (if-let [val (get (first current) symbol)]
        val
        (recur (rest current))))))

