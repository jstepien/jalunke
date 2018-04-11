(ns jalunke.eval
  (:require [jalunke.compile :as c]
            [clojure.string :as str]))

(defn call [f args]
  (apply f args))

(defn replace-with [hay needle replacement]
  (str/replace hay needle replacement))

(defn then-else [bool then else]
  (if bool
    (then)
    (else)))

(defn reverse* [str]
  (str/reverse str))

(defn reduce-with [coll f init]
  (reduce f init coll))

(defn- with-builtins [forms]
  (list
    'let
    '[call jalunke.eval/call
      then-else jalunke.eval/then-else
      reduce-with jalunke.eval/reduce-with
      reverse jalunke.eval/reverse*
      replace-with jalunke.eval/replace-with]
    forms))

(defn evaluate [code]
  (let [forms (c/compile-code code)]
    (eval (with-builtins forms))))

(comment
  (evaluate "('foo = 3)\n((foo > 0) then { \"pos\" } else { \"notpos\" })")
  (evaluate "('xs = [1 2 3])\n({|'x| (1 + x)} map xs)"))
