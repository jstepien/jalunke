(ns jalunke.grammar
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [jalunke.lexing :as lex]))

(s/def ::program
  (s/+ ::expression))

(s/def ::expression
  (s/alt ::number ::number
         ::string ::string
         ::msg-send ::msg-send
         ::function ::function
         ::nullary-function ::nullary-function
         ::bareword ::bareword
         ::assignment ::assignment
         ::array ::array))

(s/def ::msg-send
  (s/cat ::open-paren ::open-paren
         ::object ::expression
         ::method ::bareword
         ::arg-pairs (s/* (s/cat ::arg ::expression
                                 ::method ::bareword))
         ::last-arg (s/? ::expression)
         ::close-paren ::close-paren))

(s/def ::assignment
  (s/cat ::open-paren ::open-paren
         ::symbol ::unassigned-bareword
         ::eq #{[:bareword "="]}
         ::value ::expression
         ::close-paren ::close-paren))

(s/def ::array
  (s/cat ::open-bracket ::open-bracket
         ::exprs (s/* ::expression)
         ::close-bracket ::close-bracket))

(s/def ::nullary-function
  (s/cat ::open-curly ::open-curly
         ::body (s/+ ::expression)
         ::close-curly ::close-curly))

(s/def ::function
  (s/cat ::open-curly ::open-curly
         ::open-bar ::bar
         ::args (s/+ (s/alt ::unassigned-bareword
                            ::unassigned-bareword))
         ::close-bar ::bar
         ::body (s/+ ::expression)
         ::close-curly ::close-curly))

(s/def ::open-curly
  #{[:open-curly "{"]})

(s/def ::close-curly
  #{[:close-curly "}"]})

(s/def ::open-bracket
  #{[:open-bracket "["]})

(s/def ::close-bracket
  #{[:close-bracket "]"]})

(s/def ::open-paren
  #{[:open-paren "("]})

(s/def ::close-paren
  #{[:close-paren ")"]})

(s/def ::bar
  #{[:bar "|"]})

(s/def ::bareword
  (s/spec
    (s/cat ::token #{:bareword}
           ::string string?)))

(s/def ::number
  (s/spec
    (s/cat ::token #{:number}
           ::string string?)))

(s/def ::string
  (s/spec
    (s/cat ::token #{:string}
           ::string string?)))

(s/def ::unassigned-bareword
  (s/spec
    (s/cat ::token #{:unassigned-bareword}
           ::string string?)))

(defn- remove-irrelevant [tokens]
  (remove (comp #{:whitespace :comment} first) tokens))

(defn parse [code]
  (let [tokens (remove-irrelevant (lex/tokenise code))
        prog (s/conform ::program tokens)]
    (if (s/invalid? prog)
      (s/explain-data ::program tokens)
      {:program prog})))

#_
(parse "('a = ('b = 3))")
