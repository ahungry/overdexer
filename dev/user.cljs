(ns dev.user
  (:require
   [example.core :as c]))

(prn "It worked from cljs")

;; This is a special form...
(in-ns 'example.core)
