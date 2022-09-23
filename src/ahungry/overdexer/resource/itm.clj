(ns ahungry.overdexer.resource.itm
  (:require
   [gloss.core :as gc]
   [gloss.io :as gi]))

(defn get-item [s]
  (slurp (str "/home/mcarter/bgee/bgee2/override/" s)))

(defn foo []
  (prn "Yay")
  gi/decode)
