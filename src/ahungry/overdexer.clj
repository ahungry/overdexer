(ns ahungry.overdexer
  (:require
   [ahungry.overdexer.resource.itm :as itm])
  (:gen-class))

(defn x []
  (itm/foo))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (prn "Searching for the items...")
  (prn (itm/get-item-files))
  (greet {:name (first args)}))
