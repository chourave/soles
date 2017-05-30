; MIT License
;
; Copyright (c) 2017 Frederic Merizen
;
; Permission is hereby granted, free of charge, to any person obtaining a copy
; of this software and associated documentation files (the "Software"), to deal
; in the Software without restriction, including without limitation the rights
; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
; copies of the Software, and to permit persons to whom the Software is
; furnished to do so, subject to the following conditions:
;
; The above copyright notice and this permission notice shall be included in all
; copies or substantial portions of the Software.
;
; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
; SOFTWARE.

(set-env! :source-paths #{"src"})

(require '[plumula.soles.dependencies, :refer :all])
(add-dependencies!
  '(:provided
     [org.clojure/clojure "1.7.0"])
  '(:compile
     [onetom/boot-lein-generate "0.1.3"]
     [adzerk/bootlaces "0.1.13"])
  '(:test
     [boot/core "2.7.1"]))

(require '[plumula.soles, :refer :all, :exclude [add-dependencies!]])
(soles! 'plumula/soles "0.4.1" :platform :clj)

(require '[adzerk.boot-test :as test])
(task-options!
  pom       {:description "A shared bootfile for the plumula projects."
             :url         "https://github.com/plumula/soles"
             :scm         {:url "https://github.com/plumula/soles"}
             :license     {"MIT" "http://www.opensource.org/licenses/mit-license.php"}}
  test/test {:exclude     #"^plumula.soles.cljs$"})
