# https://clojure.org/reference/deps_and_cli
# A lot of this is convenience for myself, as I used to use the lazy lein
# commands, and now I've gotta learn deps.edn...

all: test

run-m:
	clojure -M:run-m "Ahungry"

# Note this one requires exec-fn is defined or it won't work
run-x:
	clojure -X:run-x :name "Ahungry"

test:
	clj -T:build test

uberjar: ci
ci:
	clj -T:build ci

deps: prepare
prepare:
	clj -P

.PHONY: test prepare
