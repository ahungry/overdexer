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
  (prn "Indexing the items, please be patient (takes ~30s or so)...")
  (time (itm/index-itm))
  ;; (prn "Indexing the dialog.tlk, please be patient (takes ~5m or so? oof)")
  ;; (time (dialog/index-dialog))
  (greet {:name (first args)})
  (System/exit 0))
