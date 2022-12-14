(ns ahungry.overdexer
  (:require
   [ahungry.overdexer.server :as server]
   [ahungry.overdexer.entity.itm :as itm]
   [ahungry.overdexer.entity.dialog :as dialog]
   [clojure.tools.logging :as log]
   [clojure.pprint]
   [clojure.string]
   )
  (:gen-class))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:override-dir data) "World") "!")))

(defn reindex
  "I don't do a whole lot ... yet."
  [{:keys [dialog-dir override-dir exit]}]
  (when override-dir
    (log/info "Indexing the items, please be patient (takes ~30s or so)...")
    (time (log/info (itm/index-itm override-dir))))
  (when-not override-dir (log/info "Skipping item index, no :override-dir provided."))
  (when dialog-dir
    (log/info "Indexing the dialog.tlk, please be patient (takes ~5m or so? oof)")
    (time (log/info (dialog/index-dialog dialog-dir))))
  (when-not dialog-dir (log/info "Skipping dialog index, no :dialog-dir provided."))
  (when exit
    (System/exit 0))
  (log/info "Indexing done, press Ctrl-c to exit if this is hanging..."))

(defn parse-opts
  "Turn a seq of strings into kw based map."
  [xs]
  (reduce
   (fn [acc [k v]] (conj acc {(try (read-string k) (catch Exception _ k))
                              (try (read-string v) (catch Exception _ v))}))
   {}
   (partition 2 xs)))

(defn -main [& args]
  (let [cmd (first args)
        opts (parse-opts (rest args))]
    (clojure.pprint/pprint {:opts opts})
    (case cmd
      "reindex" (reindex opts)
      "server" (server/start true)
      (log/info "Please choose a command: [ reindex | server ]"))))
