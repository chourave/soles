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

(def clj-pipeline-factory
  "The Clojure pipeline just runs the test suite."
  (->LanguagePipelineFactory
    :clj
    '[[adzerk/boot-test "1.2.0"]]))

(def cljs-pipeline-factory
  "The ClojureScript pipeline runs the test suite, compiles the code and serves
  the site, with live reloading.
  "
  (->LanguagePipelineFactory
    :cljs
    '[[adzerk/boot-cljs "2.0.0"]
      [adzerk/boot-cljs-repl "0.3.3"]
      [adzerk/boot-reload "0.5.1"]
      [com.cemerick/piggieback "0.2.1"]
      [crisptrutski/boot-cljs-test "0.3.0"]
      [doo "0.1.7"]
      [org.clojure/tools.nrepl "0.2.12"]
      [pandeiro/boot-http "0.8.3"]
      [weasel "0.7.0"]]))

(def common-pipeline
  "The common pipeline sets up development directories and runs the file watcher."
  (reify pipe/Pipeline
    (tasks [_]
      [{:priority 10 :task testing}
       {:priority 40 :task task/watch}])
    (set-options! [_ project version target-path]
      (boot/task-options!
        task/pom #(assoc % :project project, :version version)
        task/target #(update % :dir (fn [s] (conj s target-path)))))))

(defn conform-platform
  "Creature comfort inter"
  [platform]
  (condp #(%1 %2) platform
    not #{:clj :cljs}
    set? platform
    #{platform}))

(def ^{:doc "The active `Pipeline`. Mutated by `soles!`."} dev-pipeline)

(defn soles!
  "Configure the project for project name `project`, version
  `version-or-versions` and optionally target directory `target`.
  "
  ([project version-or-versions & {:keys [target-path platform dependencies]
                                   :or   {target-path "target"}}]
   (let [platform (conform-platform platform)
         version (if (map? version-or-versions)
                   (version-or-versions project)
                   version-or-versions)
         pipeline-factory (pipe/->CompositePipelineFactory
                            [common-pipeline clj-pipeline-factory cljs-pipeline-factory])]
     (add-dir! :source-paths "src")
     (add-dir! :resource-paths "resources")
     (boot/merge-env! :dependencies (concat (deps/dependify dependencies)
                                            (pipe/dependencies-for pipeline-factory platform)))
     (alter-var-root #'dev-pipeline (constantly (pipe/pipeline-for pipeline-factory platform)))
     (pipe/set-options! dev-pipeline project version target-path)
     (bootlaces! version)
     (lein/generate))))

(boot/deftask dev
  "Launch Immediate Feedback Development Environment."
  []
  (pipe/make-middleware dev-pipeline))

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
