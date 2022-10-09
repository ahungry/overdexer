(ns dev.user
  (:require
   [cljs.core]
   [example.core :as c]))

(prn "It worked from cljs")

(cljs.core/in-ns 'example.core)
