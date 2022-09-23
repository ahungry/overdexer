(ns ahungry.repo.itm
  (:require
   [clojure.java.jdbc :as j]))

;; https://github.com/clojure/java.jdbc
(def db
  {:dbtype "sqlite"
   :dbname "override.db"
   })

(j/query db ["select * from x"])

(defn make-table [db ]
  (j/execute! db (j/create-table-ddl "itm" [[:name "text"]])))

(j/query db ["select * from itm"])
