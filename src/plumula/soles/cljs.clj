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
    (pipeline-tasks [_]
      [{:priority 25 :task serve}
       {:priority 50 :task test-cljs}
       {:priority 80 :task #(comp (report-errors!)
                                  (reload)
                                  (cljs-repl)
                                  (cljs)
                                  (task/target))}])
    (set-pipeline-options! [_ project version target-path]
      (boot/task-options!
        serve {:dir target-path}
        test-cljs {:js-env :node, :update-fs? true, :keep-errors? true, :optimizations :simple}
        task/repl {:port 9009}
        cljs {:compiler-options {:infer-externs true}}))))
