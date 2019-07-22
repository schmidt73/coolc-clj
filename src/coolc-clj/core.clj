(ns core
  (:require [core.regex]))

; Tokens look like {:type :if} or optionally {:type :identifier :value "HTOWN"}

(def integer #"[0-9]+") ; int-const

(def object-identifier #"[a-z][a-zA-Z0-9_]*") ; object-id
(def type-identifier #"[A-Z][a-zA-Z0-9_]*") ; type-id
(def whitespace #"[ \n\f\r\t\v]+")

(defn default-action
  "Constructs an action that returns the token type along
   with the matched string."
  [token-type]
  (fn [match] {:type token-type :value match}))

(def rules
  [{:regex object-identifier :action (default-action :object-id)}
   {:regex type-identifier :action (default-action :type-id)}
   {:regex integer :action (default-action :int-const)}
   {:regex whitespace :action (constantly nil)}])

(defn lex-rule [rule str]
  (let [matcher (.matcher (:regex rule) str)]
    (when (.lookingAt matcher)
      (let [match (.group matcher)]
        {:match match :token ((:action rule) match)}))))

(defn lex-rules [rules str]
  (let [lexes (filter #(not (nil? %)) (map #(lex-rule % str) rules))
        sorted-lexes (sort-by #(count (:match %1)) lexes)]
    (first sorted-lexes)))

(defn lex [rules input]
  (loop [tokens [] str input]
    (if-let [{:match m :token t} (lex-rules rules str)]
      (recur (conj tokens t) (subs str (count m)))
      (recur (conj tokens {:type :error}) (subs str 1)))))

(lex-rules rules "12")
