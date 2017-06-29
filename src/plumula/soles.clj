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
  {:boot/export-tasks true}
  (:require [adzerk.bootlaces :refer [bootlaces! build-jar push-snapshot push-release]]
            [boot.core :as boot]
            [boot.lein :as lein]
            [boot.task.built-in :as task]
            [clojure.java.io :as io]
            [plumula.soles.dependencies :as deps]
            [plumula.soles.task-pipeline :as pipe]))

(defn add-dir!
  "Add `dir` to the `key` path in the environment, providing `dir` actually
  is a directory.
  "
  [key dir]
  (when (.isDirectory (io/file dir))
    (boot/merge-env! key #{dir})))

(defmacro import-vars
  "Make vars from the namespace `ns` available in `*ns*` too.
  They can then be `require`d from `*ns*` as if they had been defined there.

  `names` should be a sequence of symbols naming the vars to import."
  [ns names]
  `(do
     ~@(apply concat
              (for [name names
                    :let [src (symbol (str ns) (str name))]]
                `((def ~name ~(resolve src))
                  (alter-meta! (var ~name) merge (meta (var ~src))))))))

(import-vars plumula.soles.dependencies (add-dependencies!))

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

(defmacro when-handles
  "Executes the `body` if the `language` is a member of `platforms`."
  [language platforms & body]
  `(when ((keyword ~language) ~platforms)
     ~@body))

(defn platform-ns-symbol
  "If called with just one argument, return the unqualified symbol corresponding
  to the namespace that contains the pipeline for the `language`.
  If called with an extra name argument, return the qualified symbol
  corresponding to that name inside the namespace that contains the pipeline for
  the `language`"
  ([language]
   (symbol (str "plumula.soles." (name language))))
  ([language name]
   (symbol (str (platform-ns-symbol language)) name)))

(defrecord LanguagePipelineFactory [language dependencies]
  pipe/PipelineFactory
  (dependencies-for [_ platforms]
    (when-handles language platforms
      (deps/scopify [:test dependencies])))
  (pipeline-for [_ platforms]
    (when-handles language platforms
      (require (platform-ns-symbol language))
      @(resolve (platform-ns-symbol language "pipeline")))))

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
