(ns ahungry.overdexer.entity.dialog
  (:require
   [ahungry.overdexer.entity.db :as db]
   [ahungry.overdexer.entity.iesdp :as ie]
   [ahungry.overdexer.util :as util]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clojure.walk]
   [gloss.core :as c]
   [gloss.io :as io]))

;; NOTE: This may be a bit trickier to re-assemble into a proper output format
;; than the items, however I don't see any reason I would use this over the
;; weidu TRA stuff for modifying strings.

;; https://gibberlings3.github.io/iesdp/file_formats/ie_formats/tlk_v1.htm

(def header-spec
  [
   :signature                (ie/_char-array 4)
   :version                  (ie/_char-array 4)
   :language-id              (ie/word        2)
   :number-of-strref-entries (ie/dword       4)
   :offset-to-string-data    (ie/dword       4)
   ])

(def entry-spec
  [
   :bit-field       (ie/word   2)
   :sound           (ie/resref 8)
   :volume-variance (ie/dword  4)
   :pitch-variance  (ie/dword  4)
   :offset          (ie/dword  4)
   :length          (ie/dword  4)
   ])

(def string-spec
  (c/repeated
   :byte
   :prefix (c/prefix entry-spec #(get % :length) str)))

(def header-frame (apply c/ordered-map header-spec))
(def entry-frame (apply c/ordered-map entry-spec))

(defn get-dialog [dialog-dir]
  (with-open
    [in-stream (clojure.java.io/input-stream
                (str dialog-dir "/dialog.tlk")
                ;; "/home/mcarter/bgee/bgee2/lang/en_US/dialog.tlk"
                )]
    (->
     (java.nio.ByteBuffer/wrap (.readAllBytes in-stream))
     (.order java.nio.ByteOrder/LITTLE_ENDIAN))))

(defn parse-dialog [dialog-dir]
  (let [dialog (get-dialog dialog-dir)
        header (io/decode header-frame (.slice dialog 0 18))]
    {:header header

     :entries
     (pmap (fn [i]
             (let [entry (io/decode entry-frame (.slice dialog (+ 18 (* i 26)) 26))]
               {:entry entry
                :pkid i
                :string
                (let [segment (.slice dialog
                                      (+ (:offset-to-string-data header)
                                         (:offset entry))
                                      (:length entry)
                                      )]
                  (try
                    ;; Using the char-array is ~10x faster but can hit errors on some vals
                    (io/decode (ie/_char-array (:length entry)) segment)
                    (catch Exception _ (io/decode (ie/safe-string (:length entry)) segment))
                    ))}))
           (range (:number-of-strref-entries header)))}))

(defn batch-import [rows]
  ;; Insert all the top level data rows
  (j/with-db-transaction [t-con db/db]
    ;; (j/execute! db/db "PRAGMA synchronous = OFF")
    ;; (j/query db/db "PRAGMA journal_mode = MEMORY")
    (j/insert-multi! t-con "dialog" (db/fast-rows-normalizer rows))))

(defn batch-csv [rows]
  (util/rows->csv "dialog" (db/fast-rows-normalizer rows)))

(defn index-dialog [dialog-dir]
  (db/make-table-from-spec db/db "dialog" (conj entry-spec "string" nil) "int" "unique")
  (j/delete! db/db "dialog" [])
  (->> (parse-dialog dialog-dir)
       :entries
       (pmap (fn [entry] (conj {:pkid (:pkid entry)} (:entry entry) {:string (:string entry)})))
       batch-import
       ;; batch-csv
       count))
