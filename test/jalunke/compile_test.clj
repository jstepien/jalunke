(ns jalunke.compile-test
  (:require [jalunke
             [compile :as c]
             [grammar :as grammar]]
            [clojure.spec.alpha :as s]
            [clojure.test.check
             [clojure-test :refer [defspec]]
             [properties :as prop]
             [generators :as gen]]
            [clojure.test :refer :all]))

(deftest small-samples
  (are [in out] (= out (c/compile-code in))

       "/**/ 1 /* \n\n */ 2 /* \" */"
       '(do 1 2)

       "('a = 3)"
       '(clojure.core/let [a 3])

       "('a = [b])"
       '(clojure.core/let [a [b]])

       "('a = [c (3 + 1) d])"
       '(clojure.core/let [a [c (+ 3 1) d]])

       "({ \"bar\" } call)"
       '(call (clojure.core/fn [] "bar"))

       "(\"foo\" replace \"b\" with \"f\")"
       '(replace-with "foo" "b" "f")

       "{ |'a| a }"
       '(clojure.core/fn [a] a)

       "('fn = { |'a 'b| (a + b) }) (fn call [1 2])"
       '(clojure.core/let [fn (clojure.core/fn [a b]
                                (+ a b))]
          (call fn [1 2]))

       "({ 0 1 } call)"
       '(call (clojure.core/fn [] 0 1))

       "('a = (\"foo\" reverse))"
       '(clojure.core/let [a (reverse "foo")])

       "('a = 3) (a + a)"
       '(clojure.core/let [a 3]
          (+ a a))

       "(a < 1)" '(< a 1)))

(s/def ::grammar/number
  (s/with-gen (s/get-spec ::grammar/number)
    (fn []
      (->> gen/int
           (gen/fmap str)
           (gen/fmap (partial vector :number))))))

(s/def ::grammar/string
  (s/with-gen (s/get-spec ::grammar/string)
    (fn []
      (->> gen/string-alphanumeric
           (gen/fmap #(str \" % \"))
           (gen/fmap (partial vector :string))))))

#_
(s/def ::grammar/unassigned-bareword
  (s/with-gen (s/get-spec ::grammar/unassigned-bareword)
    (fn []
      (gen/return [:unassigned-bareword "'a"])
      )))

#_
(gen/sample (s/gen ::grammar/unassigned-bareword))

#_
(defspec generative-madness
  (prop/for-all
    [program (s/gen ::grammar/program)]
    (c/compile-exprs (s/conform ::grammar/program program))))
