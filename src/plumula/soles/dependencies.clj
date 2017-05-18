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

(ns plumula.soles.dependencies
  (:require [boot.core :as boot]))

(defn version?
  "Returns
  - false if the list passed as an argument is a scope + dependencies
  - true if the list the :versions keyword, marking a list of versions (as
    opposed to a list of dependencies)
  "
  [[scope]]
  (= :versions scope))

(defn map-v
  "Returns the map obtained by applying `f` to the values in the map `m`.
  The keys are left unchanged.
  "
  [m f]
  (into {} (for [[k v] m] [k (f v)])))

(defn filter-versions
  "Given a list of scoped dependencies `deps`, returns a map from dependency
  names to lists of versions.

  For instance, for the input [[:versions ['clojure \"1.3\"]]], the output will
  be {'clojure [\"1.3\"]}
  "
  [deps]
  (-> deps
      (->> (eduction (comp (filter version?) (mapcat rest)))
           (group-by first))
      (map-v #(mapcat rest %))))

(defn versionify
  "Given a map of `versions` as produced by `filter-versions`, return a function
  that takes a list specifying one dependency and
  - returns it unchanged if this dependency isn’t in `versions`
  - returns it with the corresponding entry from `versions` spliced in in second
    position otherwise
  "
  [versions]
  (fn [[dep & opts :as in]]
    (if-let [v (versions dep)]
      (reduce into [dep] [v opts])
      in)))

(defn scopify
  "Given a two-element list, where the first element is a scope keyword,
  and the second element is a list of dependency specifications, distribute
  the scope over the dependency specifications (in a boot-compatible format).
  "
  [[scope deps]]
  (map #(conj (vec %) :scope (name scope)) deps))

(defn within-scope
  "Given a function `f`, return a function that takes a list as its arguments.
  The idea is that
  - the first element of that list is a scope name,
  - and the remaining elements are all dependency specifiers.
  The function will return a two-element list, where
  - the first element is the scope, unchanged
  - the second element is a list, obtained my mapping `f` over the dependency
  specifiers"
  [f]
  (fn [[scope & deps]]
    [scope (map f deps)]))

(defn vecify
  "If the argument is sequential, return it as a vector. Else, return it
  wrapped in a vector."
  [dep]
  (condp #(%1 %2) dep
    vector? dep
    sequential? (vec dep)
    [dep]))

(defn dependify
  "Turn a soles-style dependency list into a boot-style dependency list."
  [deps]
  (let [versions (filter-versions deps)]
    (into []
          (comp (remove version?)
                (map (within-scope (comp (versionify versions)
                                         vecify)))
                (mapcat scopify))
          deps)))

(defn add-dependencies!
  "Add `deps` as depencies to the environment.

  For an example of the format, see thes use in the `build.boot` file.
  "
  [& deps]
  (boot/merge-env! :dependencies (dependify deps)))
