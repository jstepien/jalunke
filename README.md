✨
[halunke.jetzt](http://halunke.jetzt)
✨

```clojure
user=> (require 'jalunke.compile)
nil
user=> (require 'jalunke.eval)
nil
user=> (def plus (slurp "test/jalunke/plus.hal"))
#'user/plus
user=> (print plus)
('fn = { |'a 'b| (a + b) })
(fn call [1 2])
user=> (jalunke.compile/compile-code plus)
(clojure.core/let [fn (clojure.core/fn [a b] (+ a b))] (call fn [1 2]))
user=> (jalunke.eval/evaluate plus)
3
```

Copyright (c) 2018 Jan Stępień. Distributed under the MIT License.
