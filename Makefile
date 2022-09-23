all: test

test:
	clj -T:build test

uberjar: ci
ci:
	clj -T:build ci

.PHONY: test
