# https://clojure.org/reference/deps_and_cli
# A lot of this is convenience for myself, as I used to use the lazy lein
# commands, and now I've gotta learn deps.edn...
OVERRIDE_DIR?="/tmp"
DIALOG_DIR?="/tmp"

all: test

server:
	clojure -M:run-m server

reindex:
	clojure -M:run-m reindex "$(OVERRIDE_DIR)" "$(DIALOG_DIR)"

# Note this one requires exec-fn is defined or it won't work
run-x:
	clojure -X:run-x :override-dir "$(OVERRIDE_DIR)" :dialog-dir "$(DIALOG_DIR)"

test:
	clj -T:build test

uberjar: ci
ci:
	clj -T:build ci

deps: prepare
prepare:
	clj -P

.PHONY: test prepare
