(ns jalunke.lexing
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]))

(defn- single-char [code]
  (some-> (case (first code)
            \( :open-paren
            \) :close-paren
            \{ :open-curly
            \} :close-curly
            \[ :open-bracket
            \] :close-bracket
            \| :bar
            nil)
          (vector (subs code 0 1))))

(defn- number [code]
  (if-let [m (re-find #"^[+-]?[0-9]+" code)]
    [:number m]))

(defn- unassigned-bareword [code]
  (if-let [m (re-find #"^'[a-zA-Z_]+" code)]
    [:unassigned-bareword m]))

(defn- bareword [code]
  (if-let [[m] (re-find #"^([a-zA-Z_]+|[<>+=-])" code)]
    [:bareword m]))

(defn- string [code]
  (if-let [m (re-find #"^\"[^\"]+\"" code)]
    [:string m]))

(defn- whitespace [code]
  (if-let [m (re-find #"^[ \t\n]+" code)]
    [:whitespace m]))

(defn- comment-block [code]
  (if (str/starts-with? code "/*")
    (let [idx (str/index-of code "*/")]
      [:comment (subs code 0 (+ 2 idx))])))

(defn tokenise [code]
  (if (seq code)
    (if-let [[type string] (or (single-char code)
                               (number code)
                               (string code)
                               (unassigned-bareword code)
                               (bareword code)
                               (comment-block code)
                               (whitespace code))]
      (cons [type string] (tokenise (subs code (count string))))
      (throw (ex-info "Unexpected token" {:code code})))))
