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

(ns plumula.soles.cljs
  (:require [adzerk.boot-cljs :refer [cljs]]
            [adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
            [adzerk.boot-reload :refer [reload]]
            [boot.core :as boot]
            [boot.task.built-in :as task]
            [crisptrutski.boot-cljs-test :refer [test-cljs report-errors!]]
            [pandeiro.boot-http :refer [serve]]
            [plumula.soles.task-pipeline :as pipe]))

(def pipeline
  "The ClojureScript pipeline runs the test suite, compiles the code and serves
  the site, with live reloading.
  Normally built by `plumula.soles/clj-pipeline-factory`.
  "
  (reify pipe/Pipeline
    (tasks [_]
      [{:priority 25 :task serve}
       {:priority 50 :task test-cljs}
       {:priority 80 :task #(comp (report-errors!)
                                  (reload)
                                  (cljs-repl)
                                  (cljs)
                                  (task/target))}])
    (set-options! [_ project version target-path]
      (boot/task-options!
        serve #(assoc % :dir target-path)
        test-cljs #(assoc % :js-env :node, :update-fs? true, :keep-errors? true, :optimizations :simple)
        task/repl #(assoc % :port 9009)
        cljs #(assoc-in % [:compiler-options :infer-externs] true)))))
