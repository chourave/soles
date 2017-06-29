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

(def +project+ 'plumula/soles)
(def +version+ "0.5.0")

(set-env! :source-paths #{"src"})
(require '[plumula.soles.dependencies, :refer :all])

(add-dependencies!
  '(:provided
     [org.clojure/clojure "1.7.0"])
  '(:compile
     [onetom/boot-lein-generate "0.1.3"]
     [adzerk/bootlaces "0.1.13"])
  '(:test
     [adzerk/boot-test "1.2.0"]
     [boot/core "2.7.1"]))

(require  '[adzerk.boot-test :as test]
          '[adzerk.bootlaces :refer [bootlaces!]]
          '[boot.lein :as lein]
          '[plumula.soles :refer [add-dir! testing deploy-local deploy-snapshot deploy-release]])

(add-dir! :source-paths "src")

(bootlaces! +version+)
(lein/generate)

(task-options!
  pom       {:project     +project+
             :version     +version+
             :description "A shared bootfile for the plumula projects."
             :url         "https://github.com/plumula/soles"
             :scm         {:url "https://github.com/plumula/soles"}
             :license     {"MIT" "https://opensource.org/licenses/MIT"}}
  test/test {:exclude     #"^plumula.soles.cljs$"})

(deftask dev
  "Launch Immediate Feedback Development Environment."
  []
  (comp
    (testing)
    (watch)
    (test/test)))
