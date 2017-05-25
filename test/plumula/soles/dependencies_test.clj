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

(ns plumula.soles.dependencies-test
  (:require [plumula.soles.dependencies :as d]
            [clojure.test :refer [deftest is testing]]))

(deftest test-versions
  (testing "version?"
    (is (d/version? '(:versions [org.clojure/clojure "1.3.0"])))
    (is (not (d/version? '(:compile [org.clojure/clojure "1.3.0"])))))

  (testing "map-v"
    (is (= {:a 1 :b 2}
           (d/map-v {:a 0 :b 1} inc))))

  (testing "filter-versions"
    (is (= {}
           (d/filter-versions '((:compile [org.clojure/clojure "1.3.0"])))))
    (is (= {'org.clojure/clojure ["1.3.0"]}
           (d/filter-versions '((:versions [org.clojure/clojure "1.3.0"])))))
    (testing "Multiple dependencies"
      (is (= '{org.clojure/clojure       ["1.3.0"]
               org.clojure/clojurescript ["3.141592"]}
             (d/filter-versions '((:versions
                                    [org.clojure/clojure "1.3.0"]
                                    [org.clojure/clojurescript "3.141592"])))))
      (testing "One dependency appearing multiple times"
        (is (= '{org.clojure/clojure ["1.3.0" :exclusions [garble]]}
               (d/filter-versions '((:versions
                                      [org.clojure/clojure "1.3.0"]
                                      [org.clojure/clojure :exclusions [garble]]))))))
      (testing "Multiple version clauses"
        (is (= '{org.clojure/clojure       ["1.3.0"]
                 org.clojure/clojurescript ["3.141592"]}
               (d/filter-versions '((:versions [org.clojure/clojure "1.3.0"])
                                     (:versions [org.clojure/clojurescript "3.141592"])))))
        (testing "for the same dependency"
          (is (= '{org.clojure/clojure ["1.3.0" :exclusions [garble]]}
                 (d/filter-versions '((:versions [org.clojure/clojure "1.3.0"])
                                       (:versions [org.clojure/clojure :exclusions [garble]]))))))))

    (testing "versionify"
      (testing "with no external version"
        (is (= '[org.clojure/clojure "1.3.0" :exclusions [garble]]
               ((d/versionify {})
                 '(org.clojure/clojure "1.3.0" :exclusions [garble])))))
      (testing "with external version"
        (is (= '[org.clojure/clojure "1.3.0"]
               ((d/versionify '{org.clojure/clojure ["1.3.0"]})
                 '(org.clojure/clojure)))))
      (testing "mixing external version with internal info"
        (is (= '[org.clojure/clojure "1.3.0" :exclusions [garble]]
               ((d/versionify '{org.clojure/clojure ["1.3.0"]})
                 '(org.clojure/clojure :exclusions [garble]))))))))

(deftest test-scope
  (testing "scopify"
    (is (= '[[foo :scope "bar"] [void :scope "bar"]]
           (d/scopify '(:bar [(foo) (void)])))))

  (testing "within-scope"
    (is (= [:foo [2 3 4]]
           ((d/within-scope inc) [:foo 1 2 3])))))

(deftest test-vecify
  (testing "vecify"
    (testing "wraps strings"
      (let [wrapped (d/vecify "foo")]
        (is (= ["foo"] wrapped))
        (is (vector? wrapped))))
    (testing "leaves vectors alone"
      (let [a-vec [1 2 3]
            wrapped (d/vecify a-vec)]
        (is (identical? a-vec wrapped))))
    (testing "turnes seqs into vetors"
      (let [wrapped (d/vecify '("foo"))]
        (is (= ["foo"] wrapped))
        (is (vector? wrapped))))))

(deftest test-dependify
  (testing "dependify"
    (is (= '[[clojure "1" :scope "provided"]
             [openure.org/openure "2" :exclusions [warble] :scope "provided"]
             [fumble "20.6" :scope "compile"]]
           (d/dependify
             '((:versions
                 [clojure "1"]
                 [openure.org/openure "2"])
               (:provided
                 clojure
                 [openure.org/openure :exclusions [warble]])
               (:compile
                 [fumble "20.6"])))))))
