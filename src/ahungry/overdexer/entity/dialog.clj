(ns ahungry.overdexer.entity.dialog
  (:require
   [ahungry.overdexer.entity.db :refer [db]]
   [ahungry.overdexer.entity.iesdp :as ie]
   [ahungry.overdexer.util :as util]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clojure.walk]
   [gloss.core :as c]
   [gloss.io :as io]))

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

(defn get-dialog []
  (with-open
    [in-stream (clojure.java.io/input-stream
                "/home/mcarter/bgee/bgee2/lang/en_US/dialog.tlk")]
    (->
     (java.nio.ByteBuffer/wrap (.readAllBytes in-stream))
     (.order java.nio.ByteOrder/LITTLE_ENDIAN))))

(defn parse-dialog []
  (let [dialog (get-dialog)
        header (io/decode header-frame (.slice dialog 0 18))]
    {:header header

     :entries
     (map (fn [i]
            (let [entry (io/decode entry-frame (.slice dialog (+ 18 (* i 26)) 26))]
              {:entry entry
               :string (io/decode
                        (c/string :utf-8 :length (:length entry))
                        (.slice dialog
                                (+ (:offset-to-string-data header)
                                   (:offset entry))
                                (:length entry)
                                ))}))
          (range (:number-of-strref-entries header)))
     }
    ))
