# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

## [Unreleased]

## [0.3.0] – 2017-05-18

### Added
- Common versions can now be passed to `add-dependencies!` in a `:versions` key

### Removed
- Soles no longer holds shared dependency versions

### Changed
- The arguments to `add-dependencies!` must now be quoted

## [0.2.0] – 2017-05-17

### Changed
- Moved parentheses before scope rather than after it in `add-dependencies!` 
  to make similar more similar to `:require`’s
- Now the whole dependency list gets scoped, not just the dependency name
- Excluded clojure from specs dependency to avoid classpath conflict
- It is now possible to require `soles` directly, without requiring
  `soles.dependencies` first. This comes at the cost of making the `soles`
  namespace have a side effect - not sure I like that. 

## [0.1.0] – 2017-05-09

### Added
- Initiated the change log
- Documented usage in read me
- Initialised dependencies
- Shared dependency versions
- Initialised common tasks
- Initialised gitignore
- MIT license

[Unreleased]: https://github.com/plumula/soles/compare/0.3.0...HEAD
[0.3.0]: https://github.com/plumula/soles/compare/0.2.0...0.3.0
[0.2.0]: https://github.com/plumula/soles/compare/0.1.0...0.2.0
[0.1.0]: https://github.com/plumula/soles/compare/init...0.1.0
