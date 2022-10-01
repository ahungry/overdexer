(ns ahungry.overdexer
  (:require
   [ahungry.overdexer.entity.itm :as itm]
   [ahungry.overdexer.entity.dialog :as dialog]
   )
  (:gen-class))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:override-dir data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [override-dir (first args)
        dialog-dir (second args)]
    (prn {:override-dir override-dir
          :dialog-dir dialog-dir})
    (prn "Indexing the items, please be patient (takes ~30s or so)...")
    (time (prn (itm/index-itm override-dir)))
    (prn "Indexing the dialog.tlk, please be patient (takes ~5m or so? oof)")
    (time (prn (dialog/index-dialog dialog-dir)))
    ;; (greet {:name (first args)})
    (System/exit 0)))
