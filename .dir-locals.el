;; -*- mode: emacs-lisp -*-
(
 (clojure-mode
  (cider-clojure-cli-aliases . ":env/dev:test"))
 (clojurescript-mode
  (cider-clojure-cli-aliases . ":env/dev:test")
  (eval . (cider-register-cljs-repl-type 'figwheel-main "(do (require 'figwheel.main) (figwheel.main/start :dev))"))
  ;; (cider-default-cljs-repl . super-cljs)
  ))
