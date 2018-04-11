(ns jalunke.eval-test
  (:require [jalunke.eval :as e]
            [clojure.spec.alpha :as s]
            [clojure.test.check
             [clojure-test :refer [defspec]]
             [properties :as prop]
             [generators :as gen]]
            [clojure.test :refer :all]))

(deftest happy-path
  (are [in out] (= out (e/evaluate in))

       "('fn = { |'a 'b| (a + b) }) (fn call [1 2])"
       3

       "((2 > 3) then { \"yes!\" } else { \"no!\" })"
       "no!"

       "(\"abc\" reverse)"
       "cba"

       "([1 2 3] reduce  {|'memo 'el|  (memo + el) } with 0)"
       6

       "(\"foo\" replace \"f\" with \"b\")"
       "boo"))
