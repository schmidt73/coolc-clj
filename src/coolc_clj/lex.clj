(ns cooljc-clj.lex
  "A rule consists of a regex to match against an action
   to take upon successful regex match. This action is a function
   that takes the match and the input string as arguments and
   returns a token.

   The action can signal failure (i.e. rule does not apply)
   by returning nil.

   Tokens have the structure: {:type T :chop N ?:value V}
      where
        :type specifies the type of token
        :chop specifies how much of the input the lexer should eat
        :value optionally specifies a value for the token ")

(defn lex-rule 
  "Attempts to lex a prefix of the string with
   the passed in rule. Returns nil on failure."
  [rule str]
  (let [matcher (.matcher (:regex rule) str)]
    (when (.lookingAt matcher)
      (let [match (.group matcher)]
        ((:action rule) match str)))))

(defn lex-rules
  "Attempts to lex a prefix of the string using the rules.

   The rule that finds the longest match found is always the one
   used. If multiple rules find a longest match, the rule listed
   first in the sequence is used."
  [rules str]
  (let [lexes (filter #(not (nil? %)) (map #(lex-rule % str) rules))
        sorted-lexes (sort-by #(:chop %1) > lexes)]
    (first sorted-lexes)))

(defn lex
  "Takes a sequence of rules, an input string, and lexes it into a
   vector of tokens. Places error tokens where it fails to find
   a matching rule."
  [rules input]
  (loop [tokens [] str input]
    (if (empty? str)
      tokens
      (if-let [t (lex-rules rules str)]
        (recur (conj tokens t) (subs str (:chop t)))
        (recur (conj tokens {:type :error}) (subs str 1))))))

(defn match-action
  "Constructs an action that returns the token type along
   with the matched string."
  [token-type]
  (fn [match _] {:type token-type :value match :chop (count match)}))

(defn nomatch-action
  "Constructs an action that returns the token type without
   the matched string."
  [token-type]
  (fn [match _] {:type token-type :chop (count match)}))

(defn keyword-rule
  "Constructs a rule for the given keyword."
  [name]
  {:regex (re-pattern (str "(?i)" name))
   :action (nomatch-action (keyword name))})

(def keyword-rules
  (concat
    (map keyword-rule
      ["class" "else" "fi" "if" "in" "inherits"
       "isvoid" "let" "loop" "pool" "then" "while"
       "case" "esac" "new" "of" "not"])
    [{:regex #"t(?i)rue" :action (constantly {:type :boolean :value true :chop 4})}
     {:regex #"f(?i)alse" :action (constantly {:type :boolean :value false :chop 5})}]))

(def operation-rules
  [{:regex #"\*" :action (nomatch-action :multiply)}
   {:regex #"/"  :action (nomatch-action :divide)}
   {:regex #"\+" :action (nomatch-action :add)}
   {:regex #"-"  :action (nomatch-action :subtract)}
   {:regex #"<"  :action (nomatch-action :less-than)}
   {:regex #"="  :action (nomatch-action :equal)}
   {:regex #"<=" :action (nomatch-action :less-than-equal)}
   {:regex #"~"  :action (nomatch-action :complement)}])

;; strings and comments cannot be recognized
;; by regular languages (as it involves balanced
;; parentheses) thus we need special actions for
;; them :(

(defn process-escape
  [input]
  (when (not (empty? (rest input)))
    (let [c (first (rest input))]
      (cond
        (= c \b) {:chop 2 :replace "\b"}
        (= c \t) {:chop 2 :replace "\t"}
        (= c \n) {:chop 2 :replace "\n"}
        (= c \f) {:chop 2 :replace "\f"}
        (= c \newline) {:chop 2 :replace ""}
        :otherwise {:chop 2 :replace (str c)}))))

(defn process-chunk
  [input]
  (let [c (first input)]
    (cond
      (contains? #{\newline \u0000 \"} c) nil
      (= c \\) (process-escape input)
      :otherwise {:chop 1 :replace (str c)})))

(defn string-action
  [_ input]
  (loop [index 1 output ""]
    (let [chunk (subs input index)]
      (when (not (empty? chunk))
        (if-let [{chop :chop replace :replace} (process-chunk chunk)]
          (recur (+ index chop) (str output replace))
          (when (= (first chunk) \")
            {:chop (+ index 1) :value output :type :string-const}))))))

(defn comment-action
  [_ input]
  (loop [num-parens 1 index 2]
    (if (= num-parens 0)
      {:chop index :type :comment}
      (let [chunk (subs input index)]
        (when (>= (count chunk) 2)
          (let [cs (subs chunk 0 2)]
            (cond
              (= cs "(*") (recur (+ num-parens 1) (+ index 2))
              (= cs "*)") (recur (- num-parens 1) (+ index 2))
              :otherwise (recur num-parens (+ index 1)))))))))

(def string-rule
  {:regex #"\""
   :action string-action})

(def comment-rule
  {:regex #"\(\*"
   :action comment-action})

(def rules
  (concat
    operation-rules
    keyword-rules
    [string-rule
     comment-rule]
    [{:regex #"[a-z][a-zA-Z0-9_]*" :action (match-action :object-id)}
     {:regex #"[A-Z][a-zA-Z0-9_]*" :action (match-action :type-id)}
     {:regex #"[0-9]+"             :action (match-action :int-const)}
     {:regex #"[ \n\f\r\t\v]+"     :action (nomatch-action :whitespace)}
     {:regex #"\{"                 :action (nomatch-action :left-bracket)}
     {:regex #"\}"                 :action (nomatch-action :right-bracket)}
     {:regex #"\("                 :action (nomatch-action :left-paren)}
     {:regex #"\)"                 :action (nomatch-action :right-paren)}
     {:regex #";"                  :action (nomatch-action :semicolon)}
     {:regex #":"                  :action (nomatch-action :colon)}
     {:regex #"@"                  :action (nomatch-action :at)}
     {:regex #","                  :action (nomatch-action :comma)}
     {:regex #"\."                 :action (nomatch-action :dot)}
     {:regex #"=>"                 :action (nomatch-action :double-arrow)}
     {:regex #"<-"                 :action (nomatch-action :assign)}
     {:regex #"--.*"               :action (nomatch-action :comment)}]))

(defn print-tokens
  [tokens]
  (doseq [x (->> tokens
              (filter #(not (= :whitespace (:type %))))
              (map #(dissoc % :chop)))]
    (println (str x))))
