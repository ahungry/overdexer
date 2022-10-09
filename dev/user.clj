(ns user
  (:require
   [clojure.main]
   [clojure.tools.logging :as log]
   [ahungry.overdexer :as od]
   [ahungry.overdexer.server :as s]))

(prn "Greetings from clj")

(defn go []
  (apply require clojure.main/repl-requires))

(go)

(s/start)
