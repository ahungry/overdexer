(ns user
  (:require
   [clojure.main]
   [ahungry.overdexer :as od]))

(defn go []
  (apply require clojure.main/repl-requires))

(go)