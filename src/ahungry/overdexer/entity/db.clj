(ns ahungry.overdexer.entity.db
  (:require
   [ahungry.overdexer.util :as util]
   [clojure.java.jdbc :as j]
   [clojure.string]
   [clojure.walk]))

;; https://github.com/clojure/java.jdbc
(def db
  {:dbtype "sqlite"
   :dbname "override.db"
   ;; :pragma {:synchronous "OFF"
   ;;          :journal_mode "MEMORY"}
   })

(defn key->column [s]
  (clojure.string/replace (name s) #"[^A-Za-z0-9]" "_"))

(defn val->column-type [x]
  (if (or (> (.indexOf [:byte :uint16-le :uint32-le] x) 0)
          (= (:type x) :strref))
    "int"
    "text"))

(defn spec->columns [spec]
  (map (fn [k v]
         [(key->column k) (val->column-type v)])
       (keys (apply assoc {} spec))
       (vals (apply assoc {} spec))
       ))

(defn make-table-from-spec [db name spec pk-type]
  (let [ddl (j/create-table-ddl
             name
             (conj (spec->columns spec) ["pkid" pk-type "primary_key"])
             {:conditional? true})]
    ;; (prn ddl)
    (j/execute! db ddl)))

(defn normalize-keyword [kw]
  (key->column kw))

(defn normalize-type [m]
  (cond
    (= "resref" (get m "type")) (util/get-resref (get m "val"))
    (= "strref" (get m "type")) (get m "val")
    (vector? (get m "val")) (str (get m "val"))
    (map? m) (get m "val")
    :else m))

(defn rows-normalizer [rows]
  (clojure.walk/postwalk
   (fn [x]
     (cond (keyword? x) (normalize-keyword x)
           (get x "type") (normalize-type x)
           :else x)) rows))

(defn fast-normalize-type [m]
  (cond
    (= :resref (get m :type)) (util/get-resref (get m :val))
    (= :strref (get m :type)) (get m :val)
    (vector? (get m :val)) (str :val)
    (map? m) (get m :val)
    :else m))

(defn fast-rows-normalizer [rows]
  (pmap
   (fn [row]
     (reduce-kv (fn [acc k v] (conj acc {(key->column k) (fast-normalize-type v)})) {} row))
   rows))
