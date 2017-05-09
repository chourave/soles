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

(ns plumula.soles
  (:require [adzerk.boot-cljs :refer [cljs]]
            [adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
            [adzerk.boot-reload :refer [reload]]
            [adzerk.boot-test :as test]
            [adzerk.bootlaces :refer [bootlaces! build-jar push-snapshot push-release]]
            [boot.core :as boot]
            [boot.task.built-in :as task]
            [boot.lein :as lein]
            [crisptrutski.boot-cljs-test :refer [test-cljs report-errors!]]
            [clojure.java.io :as io]
            [pandeiro.boot-http :refer [serve]]
            [plumula.soles.dependencies :refer [dependency-versions]]))

(defn add-dir!
  "Add `dir` to the `key` path in the environment, providing `dir` actually
  is a directory.
  "
  [key dir]
  (when (.isDirectory (io/file dir))
    (boot/merge-env! key #{dir})))

(defn soles!
  "Configure the project for project name `project`, version `version` and
  optionally target directory `target`.
  "
  ([project]
   (soles! project (dependency-versions project)))
  ([project version]
   (soles! project version "target"))
  ([project version target-path]
   (add-dir! :source-paths "src")
   (add-dir! :resource-paths "resources")
   (boot/task-options!
     task/pom {:project project, :version version}
     serve {:dir target-path}
     test-cljs {:js-env :node, :update-fs? true, :keep-errors? true, :optimizations :simple}
     task/repl {:port 9009}
     cljs {:compiler-options {:infer-externs true}}
     task/target {:dir #{target-path}})
   (bootlaces! version)
   (lein/generate)))

(boot/deftask testing
  "Profile setup for running tests."
  []
  (add-dir! :source-paths "test")
  (add-dir! :resource-paths "dev-resources")
  identity)

(boot/deftask old
  "List dependencies that have newer versions available."
  []
  (task/show :updates true))

(boot/deftask dev
  "Launch Immediate Feedback Development Environment."
  []
  (comp
    (testing)
    (serve)
    (task/watch)
    (test-cljs)
    (test/test)
    (report-errors!)
    (reload)
    (cljs-repl)
    (cljs)
    (task/target)))

(boot/deftask deploy-local
  "Deploy project to local maven repository."
  []
  (build-jar))

(boot/deftask deploy-snapshot
  "Deploy snapshot version of project to clojars."
  []
  (comp
    (build-jar)
    (push-snapshot)))

(boot/deftask deploy-release
  "Deploy release version of project to clojars."
  []
  (comp
    (build-jar)
    (push-release)))
