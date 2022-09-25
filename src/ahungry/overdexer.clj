(ns ahungry.overdexer
  (:require
   [ahungry.overdexer.entity.itm :as itm]
   [ahungry.overdexer.entity.dialog :as dialog]
   )
  (:gen-class))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (prn "Indexing the items...")
  (time (itm/index-itm))
  (time (dialog/index-dialog))
  ;; (prn "Searching for the items...")
  ;; (time (prn (count (itm/get-item-files-sequentially))))
  ;; (time (prn (count (itm/get-item-files))))
  (greet {:name (first args)})
  (System/exit 0))
