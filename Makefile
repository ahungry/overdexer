# https://clojure.org/reference/deps_and_cli
# A lot of this is convenience for myself, as I used to use the lazy lein
# commands, and now I've gotta learn deps.edn...
OVERRIDE_DIR?="nil"
DIALOG_DIR?="nil"

all: test

server:
	clojure -M:run-m server

#	clj -M -m figwheel.main -b dev -r
figwheel:
	clojure -M:build-dev

reindex:
	clojure -M:run-m reindex :override-dir "$(OVERRIDE_DIR)" :dialog-dir "$(DIALOG_DIR)" :exit true

# Note this one requires exec-fn is defined or it won't work
run-x:
	clojure -X:run-x :override-dir "$(OVERRIDE_DIR)" :dialog-dir "$(DIALOG_DIR)"

test:
	clj -T:build test

# Builds the bundle resource via figwheel, then creates an uberjar
uberjar: ci
ci:
	clj -m figwheel.main -o resources/public/js/bundle.js -O advanced -bo prod
	clj -T:build ci

deps: prepare
prepare:
	clj -P

.PHONY: test prepare
