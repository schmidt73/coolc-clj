(ns cooljc-clj.lex)

(defn lex-rule 
  "Attempts to lex a prefix of the string with
   the passed in rule. Returns nil on failure."
  [rule str]
  (let [matcher (.matcher (:regex rule) str)]
    (when (.lookingAt matcher)
      (let [match (.group matcher)]
        {:match match :token ((:action rule) match str)}))))

(defn lex-rules
  "Attempts to lex a prefix of the string using the rules.

   The rule that finds the longest match found is always the one
   used. If multiple rules find a longest match, the rule listed
   first in the sequence is used."
  [rules str]
  (let [lexes (filter #(not (nil? %)) (map #(lex-rule % str) rules))
        sorted-lexes (sort-by #(count (:match %1)) > lexes)]
    (first sorted-lexes)))

(defn lex
  "Takes a sequence of rules, an input string, and lexes it into a
   vector of tokens. Places error tokens where it fails to find
   a matching rule."
  [rules input]
  (loop [tokens [] str input]
    (if (empty? str)
      tokens
      (if-let [{m :match t :token} (lex-rules rules str)]
        (recur (conj tokens t) (subs str (count m)))
        (recur (conj tokens {:type :error}) (subs str 1))))))

(defn match-action
  "Constructs an action that returns the token type along
   with the matched string."
  [token-type]
  (fn [match _] {:type token-type :value match}))

(defn nomatch-action
  "Constructs an action that returns the token type without
   the matched string."
  [token-type]
  (fn [match _] {:type token-type}))

(defn keyword-rule
  "Constructs a rule for the given keyword."
  [name]
  {:regex (re-pattern (str "(?i)" name))
    :action (nomatch-action (keyword name))})

(def keyword-rules
  (map keyword-rule
    ["class" "else" "fi" "if" "in" "inherits"
     "isvoid" "let" "loop" "pool" "then" "while"
     "case" "esac" "new" "of" "not"]))

(def operation-rules
  [{:regex #"\*" :action (nomatch-action :multiply)}
   {:regex #"/"  :action (nomatch-action :divide)}
   {:regex #"\+" :action (nomatch-action :add)}
   {:regex #"-"  :action (nomatch-action :subtract)}
   {:regex #"<"  :action (nomatch-action :less-than)}
   {:regex #"="  :action (nomatch-action :equal)}
   {:regex #"<=" :action (nomatch-action :less-than-equal)}])

(def rules
  (concat
    operation-rules
    keyword-rules
    [{:regex #"[a-z][a-zA-Z0-9_]*" :action (match-action :object-id)}
     {:regex #"[A-Z][a-zA-Z0-9_]*" :action (match-action :type-id)}
     {:regex #"[0-9]+"             :action (match-action :int-const)}
     {:regex #"[ \n\f\r\t\v]+"     :action (nomatch-action :whitespace)}
     {:regex #"\{"                 :action (nomatch-action :left-paren)}
     {:regex #"\}"                 :action (nomatch-action :right-paren)}]))

(lex rules "ClASS inherits { 17 * 0123 + 69 - 72 / 4 }")
