;; -*- mode: emacs-lisp; -*-

;;; Directory Local Variables
;;; For more information see (info "(emacs) Directory Variables")


((clojure-mode . ((cider-clojure-cli-aliases . ":env/dev:test")
                  (cider-cljs-repl-type . figwheel-main)
                  (eval . (cider-register-cljs-repl-type 'figwheel-main "(do (require 'figwheel.main) (figwheel.main/start :dev))"))))
 (clojurescript-mode . ((cider-clojure-cli-aliases . ":env/dev:test")
                        (cider-cljs-repl-type . figwheel-main)
                        (eval . (cider-register-cljs-repl-type 'figwheel-main "(do (require 'figwheel.main) (figwheel.main/start :dev))")))))
