(ns plumula.soles.dependencies
  (:require [boot.core :as boot]))

(def dependency-versions
 {'adzerk/boot-cljs "2.0.0"
  'adzerk/boot-cljs-repl "0.3.3"
  'adzerk/boot-reload "0.5.1"
  'adzerk/boot-test "1.2.0"
  'adzerk/bootlaces "0.1.13"
  'cljsjs/google-diff-match-patch "20121119-2"
  'cljsjs/quill "1.1.10-0"
  'com.cemerick/piggieback "0.2.1"
  'com.sksamuel.diff/diff "1.1.11"
  'crisptrutski/boot-cljs-test "0.3.0"
  'doo "0.1.7"
  'onetom/boot-lein-generate "0.1.3"
  'org.clojure/clojure "1.9.0-alpha16"
  'org.clojure/clojurescript "1.9.521"
  'org.clojure/test.check "0.9.0"
  'org.clojure/tools.nrepl "0.2.13"
  'pandeiro/boot-http "0.8.0"
  'plumula/delta "0.1.0-SNAPSHOT"
  'plumula/diff "0.1.0-SNAPSHOT"
  'plumula/mimolette "0.1.0-SNAPSHOT"
  'plumula/plumula "0.1.0-SNAPSHOT"
  'plumula/soles "0.1.0-SNAPSHOT"
  'swiss-arrows "1.0.0"
  'weasel "0.7.0"})

(defmacro add-dependencies!
  "Add `deps` as depencies to the environment.

  For an example of the format, see `add-base-dependencies`.
  "
  [& deps]
  (letfn [(versionify [dep] (if (sequential? dep) dep [dep (dependency-versions dep)]))
          (versionify-scoped [[scope deps]] [scope (map versionify deps)])
          (scopify [[scope deps]] (map #(conj % :scope (name scope)) deps))
          (quotify [[dep & rest]] (into [`(quote ~dep)] rest))
          (dependify [deps] (->> deps (into [] (comp (partition-all 2)
                                                     (map versionify-scoped)
                                                     (mapcat scopify)
                                                     (map quotify)))))]
    `(boot/merge-env! :dependencies ~(dependify deps))))

(defn add-base-dependencies!
  "Register the baseline project dependencies for a plumula project."
  []
  (add-dependencies!
    :provided [org.clojure/clojure
               org.clojure/clojurescript]

    :test [adzerk/boot-cljs
           adzerk/boot-cljs-repl
           adzerk/boot-reload
           adzerk/boot-test
           adzerk/bootlaces
           com.cemerick/piggieback
           crisptrutski/boot-cljs-test
           doo
           onetom/boot-lein-generate
           org.clojure/tools.nrepl
           pandeiro/boot-http
           weasel]))
