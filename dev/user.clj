(ns user
  (:require
   [clojure.main]
   [clojure.tools.logging :as log]
   [ahungry.overdexer :as od]
   [ahungry.overdexer.server :as s]))

(defn go []
  (apply require clojure.main/repl-requires))

(go)

(prn "It worked")

(s/start)
