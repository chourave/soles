(ns plumula.soles.dependencies
  (:require [boot.core :as boot]))

(defmacro add-dependencies! 
  "Add `deps` as depencies to the environment.

  For an example of the format, see `add-base-dependencies`."
  [& deps]
  (letfn [(scopify [[scope deps]] (map #(conj % :scope (name scope)) deps))
          (quotify [[dep & rest]] (into [`(quote ~dep)] rest))
          (dependify [deps] (->> deps (into [] (comp (partition-all 2) (mapcat scopify) (map quotify)))))]
    `(boot/merge-env! :dependencies ~(dependify deps))))

(defn add-base-dependencies!
  []
  (add-dependencies!
    :provided [[org.clojure/clojure "1.9.0-alpha16"]
               [org.clojure/clojurescript "1.9.521"]]

    :test [[adzerk/boot-cljs "2.0.0"]
           [adzerk/boot-cljs-repl "0.3.3"]
           [adzerk/boot-reload "0.5.1"]
           [adzerk/boot-test "1.2.0"]
           [adzerk/bootlaces "0.1.13"]
           [com.cemerick/piggieback "0.2.1"]
           [crisptrutski/boot-cljs-test "0.3.0"]
           [doo "0.1.7"]
           [onetom/boot-lein-generate "0.1.3"]
           [org.clojure/tools.nrepl "0.2.13"]
           [pandeiro/boot-http "0.8.0"]
           [weasel "0.7.0"]]))
