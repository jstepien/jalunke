(ns jalunke.compile
  (:require [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [jalunke.grammar :as grammar]))

(declare compile-exprs)

(defn prepending [forms]
  (fn [more]
    (list* forms more)))

(defmulti translate first)

(defmethod translate ::grammar/number
  [[_ {string ::grammar/string}]]
  (prepending (Long/parseLong string)))

(defmethod translate ::grammar/string
  [[_ {string ::grammar/string}]]
  (prepending (subs string 1 (dec (count string)))))

(defmethod translate ::grammar/bareword
  [[_ {string ::grammar/string}]]
  (prepending (symbol string)))

(defmethod translate ::grammar/assignment
  [[_ {sym ::grammar/symbol, value ::grammar/value}]]
  (let [value (compile-exprs [value])
        sym (-> sym
                ::grammar/string
                (subs 1)
                symbol)]
    (fn [form]
      (list
        `(let [~sym ~value]
           ~@form)))))

(defmethod translate ::grammar/msg-send
  [[_ {::grammar/keys [body object method arg-pairs last-arg]}]]
  (let [method-chunks (cons method (map ::grammar/method arg-pairs))
        method (->> method-chunks
                    (map ::grammar/string)
                    (str/join \-)
                    symbol)
        args (mapv ::grammar/arg arg-pairs)
        all-args (cons object (if last-arg
                                (conj args last-arg)
                                args))
        compiled-args (map (comp compile-exprs vector)
                           all-args)]
    (prepending (cons method compiled-args))))

(defn do? [forms]
  (and (sequential? forms)
       (= 'do (first forms))))

(defmethod translate ::grammar/array
  [[_ {exprs ::grammar/exprs}]]
  (let [values (compile-exprs exprs)]
    (prepending  (if (do? values)
                   (vec (rest values))
                   [values]))))

(defmethod translate ::grammar/nullary-function
  [[_ {body ::grammar/body}]]
  (let [values (compile-exprs body)]
    (prepending `(fn []
                   ~@(if (do? values)
                       (rest values)
                       (list values))))))

(defn- compile-arguments [arg-tokens]
  {:pre [(every? (comp #{:unassigned-bareword}
                       ::grammar/token
                       second)
                 arg-tokens)]}
  (for [token arg-tokens]
    (-> token
        second
        ::grammar/string
        (subs 1)
        symbol)))

(defmethod translate ::grammar/function
  [[_ {args ::grammar/args, body ::grammar/body}]]
  (let [values (compile-exprs body)
        args (compile-arguments args)]
    (prepending `(fn ~(vec args)
                   ~@(if (do? values)
                       (rest values)
                       (list values))))))

(defmethod translate :default
  [form]
  (throw (ex-info (format  "Cannot translate %s" (pr-str form) )
                  {:form form})))

(defn compile-exprs [exprs]
  (letfn [(maybe-do [forms]
            (if (= 1 (count forms))
              (first forms)
              (cons 'do forms)))]
    (->> exprs
         (map translate)
         reverse
         (reduce (fn [cont form-fn]
                   (form-fn cont))
                 ())
         maybe-do)))

(defn compile-code [code]
  (let [result (grammar/parse code)]
    (compile-exprs (:program result))))

(comment
  (compile-code "('foo = -3)\n((foo > 0) then { \"pos\" } else { \"notpos\" })")
  (compile-code "('xs = [1 2 3])\n({|'x| (1 + x)} map xs)"))
