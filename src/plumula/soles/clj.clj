(ns plumula.soles.clj
  (:require [adzerk.boot-test :as test]
            [plumula.soles.task-pipeline :as pipe]))

(def pipeline
  "The Clojure pipeline just runs the test suite.
  Normally built by `plumula.soles/clj-pipeline-factory`.
  "
  (reify pipe/Pipeline
    (pipeline-tasks [_]
      [{:priority 60 :task test/test}])
    (set-pipeline-options! [_ project version target-path])))
