(set-env! :source-paths #{"src"})
(require '[plumula.soles.dependencies :refer [add-base-dependencies!]])
(add-base-dependencies!)
(require '[plumula.soles :refer :all])

(soles! 'plumula/soles)
