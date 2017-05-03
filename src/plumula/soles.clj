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
            [pandeiro.boot-http :refer [serve]]))

(defn add-dir!
  ""
  [key dir]
  (when (.isDirectory (io/file dir))
    (boot/merge-env! key #{dir})))

(defn soles!
  ""
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
