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

(ns plumula.soles.task-pipeline
  (:require [boot.core :as boot]))

(defprotocol PipelineFactory
  "Lists the required boot dependencies, and build a pipeline for a given
  platform. Both functions will typically return nil if none of the requested
  platforms is supported by this pipeline factory."
  (pipeline-dependencies-for [this platforms]
    "Returns a list of dependencies that need to be present for this pipeline to
    work on the `platforms`. The returned list is in a format suitable for use
    in boot’s `:dependencies` environment.
    `platforms` should be a set of keywords.")
  (pipeline-for [this platforms]
    "Returns a `Pipeline` customised for the `platforms`. The dependencies
    listed in `pipeline-dependencies-for` must have been loaded before calling
    this function. `platforms` should be a set of keywords."))

(defprotocol Pipeline
  "A pipeline is a sequence of prioritised tasks, as well as default
  options for boot tasks"
  (pipeline-tasks [this]
    "Returns the sequence of prioritised tasks. The task is under
    the `:task` key, and the priority under the `:priority` key.")
  (set-options! [this project version target-path]
    "Configure boot tasks with default parameters."))

(extend-protocol Pipeline
  nil
  (pipeline-tasks [_])
  (set-options! [_ _ _ _]))

(defn dependencies
  "Returns the dependencies required for building a pipeline for agiven platform."
  [pipeline-or-factory platforms]
  (when (satisfies? PipelineFactory pipeline-or-factory)
    (pipeline-dependencies-for pipeline-or-factory platforms)))

(defn make-pipeline
  "Build a pipeline for a given platform."
  [pipeline-or-factory platforms]
  (condp satisfies? pipeline-or-factory
    Pipeline pipeline-or-factory
    PipelineFactory (pipeline-for pipeline-or-factory platforms)))

(defrecord CompositePipeline [pipelines]
  Pipeline
  (pipeline-tasks [this]
    (mapcat pipeline-tasks pipelines))
  (set-options! [this project version target-path]
    (dorun (map #(set-options! % project version target-path) pipelines))))

(defrecord CompositePipelineFactory [pipelines-and-factories]
  PipelineFactory
  (pipeline-dependencies-for [_ platforms]
    (mapcat #(dependencies % platforms) pipelines-and-factories))
  (pipeline-for [_ platforms]
    (->CompositePipeline (map #(make-pipeline % platforms) pipelines-and-factories))))

(defn make-middleware
  "Returns the middleware for a `pipeline`"
  [pipeline]
  (->> (pipeline-tasks pipeline)
       (sort-by :priority)
       (map #((:task %)))
       (apply comp)))
