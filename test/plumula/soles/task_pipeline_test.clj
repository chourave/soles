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

(ns plumula.soles.task-pipeline-test
  (:require [plumula.soles.task-pipeline :as tp]
            [boot.core :as boot]
            [clojure.test :refer [deftest is testing]]))

(boot/deftask testing-task
  "A fake task for testing purposes"
  [p param PARAM int "A testing parameter"]
  (fn [next-handler]
    (fn [fileset]
      (next-handler (conj fileset :testing param)))))

(boot/deftask another-task
  "Another fake task for testing purposes"
  [p param PARAM int "A testing parameter"]
  (fn [next-handler]
    (fn [fileset]
      (next-handler (conj fileset :another param)))))

(deftest test-pipeline
  (testing "Build and run a 1-element pipeline"
    (boot/task-options! testing-task {:param 42})
    (let [pipeline (reify tp/Pipeline
                     (tasks [_]
                       [{:priority 10 :task testing-task}])
                     (set-options! [_ _ _ _]))
          middleware (tp/make-middleware pipeline)]
      (is (= [:testing 42]
             ((middleware identity) [])))))
  (testing "Build tasks are sorted by priority"
    (boot/task-options! testing-task {:param 42})
    (boot/task-options! another-task {:param 54})
    (let [pipeline (reify tp/Pipeline
                     (tasks [_]
                       [{:priority 20 :task another-task}
                        {:priority 10 :task testing-task}])
                     (set-options! [_ _ _ _]))
          middleware (tp/make-middleware pipeline)]
      (is (= [:testing 42 :another 54]
             ((middleware identity) []))))
    (let [pipeline (reify tp/Pipeline
                     (tasks [_]
                       [{:priority 10 :task testing-task}
                        {:priority 20 :task another-task}])
                     (set-options! [_ _ _ _]))
          middleware (tp/make-middleware pipeline)]
      (is (= [:testing 42 :another 54]
             ((middleware identity) [])))))
  (testing "Task options are used even if they were set after building the pipeline"
    (boot/task-options! testing-task {:param 42})
    (let [pipeline (reify tp/Pipeline
                     (tasks [_]
                       [{:priority 10 :task testing-task}])
                     (set-options! [_ _ _ _]))
          _ (boot/task-options! testing-task {:param 54})
          middleware (tp/make-middleware pipeline)]
      (is (= [:testing 54]
             ((middleware identity) []))))))
