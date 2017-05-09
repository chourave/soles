# soles

[](dependency)
```clojure
[plumula/soles "0.1.0"] ;; latest release
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


### Useful tasks

#### Launch development environment
Watch the sources for changes and continuously
- run a NREPL server on localhost:9009
- serve the application on [localhost:3000](http://localhost:3000)
- run the test suite on ClojureScript
- run the test suite on Clojure
- if the tests are successful, redeploy the application to the server on port
  3000 (with live reloading)

```bash
boot dev
```

Connect to the Clojure REPL with boot (or do it from
[Cursive](https://cursive-ide.com/userguide/repl.html#remote-repls) or
[Cider](https://github.com/boot-clj/boot/wiki/Cider-REPL))
```bash
boot repl -c
```

To connect to a ClojureScript browser-based REPL, start it from the Clojure REPL
and point your browser to [localhost:3000](http://localhost:3000).
```clj
(start-repl)
```

#### Build and install project jar file
```bash
boot deploy-local
```

#### Check for outdated dependencies
```bash
boot old
```

#### Deploy to clojars

Deploy a snapshot to [Clojars](https://clojars.org/):
```bash
boot deploy-snapshot
```

Deploy a release to Clojars:
```bash
boot deploy-release
```

#### To avoid entering the Clojars credentials every time

You can store you credentials in environment variables. 
The security of doing so is dubious at best.
The password may easily end up in your bash history file (or equivalent), and
the environment variables can be seen by the `ps e` command.
So, on a trusted, single-user box, this might not be a terrible idea. Maybe.
Everywhere else: stay off.

```bash
export CLOJARS_USER=me
export CLOJARS_PASS=sekr1t
```

## Known limitations

The ClojureScript tests are currently compiled with simple optimizations, which
is fairly slow. For some reason node is unhappy when optimizations are set to
none, and alternate runners fare no better.


## Change log

The notable changes to this project are documented in the [change log](CHANGELOG.md).


## License

Distributed under the [MIT license](LICENSE.txt).
Copyright &copy; 2017 [Frederic Merizen](https://www.linkedin.com/in/fredericmerizen/).
