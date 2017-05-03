# soles

[](dependency)
```clojure
[plumula/soles "0.1.0-SNAPSHOT"] ;; latest release
```
[](/dependency)

An attempt to factor out the commonalities between the bootfiles of the
various plumula projects.

I have no intent to maintain a public stable interface, so you probably
shouldn’t depend on this. On the other hand, feel free to look around
and copy whatever you find useful.

## Usage

Add `soles` to your `build.boot` dependencies, `require` the namespace,
and let it add its dependencies:

```clj
(set-env! :dependencies [['plumula/soles "X.Y.Z" :scope "test"]])
(require '[plumula.soles.dependencies :refer [add-dependencies! add-base-dependencies!]])
(add-base-dependencies!)
(require '[plumula.soles :refer :all])
```

Then initialize soles with the project name and version, and add your own dependencies:

```clj
(soles! 'plumula/mimolette "0.1.0-SNAPSHOT")

(add-dependencies!
  :compile [[org.clojure/test.check "0.9.0"]])
```
