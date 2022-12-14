(ns ahungry.overdexer.entity.itm
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

;; https://gibberlings3.github.io/iesdp/file_formats/general.htm
;; https://gibberlings3.github.io/iesdp/file_formats/ie_formats/itm_v1.htm

;; https://github.com/clj-commons/gloss/wiki/Introduction
;; https://cljdoc.org/d/org.clj-commons/gloss/0.3.0/api/gloss
;; https://github.com/d-t-w/by-example-gloss/blob/master/src/by_example_gloss/core.clj

(def header-spec
  [
   :signature                  (ie/_char-array 4)
   :version                    (ie/_char-array 4)
   :unidentified-name          (ie/strref      4)
   :identified-name            (ie/strref      4)
   :replacement-item           (ie/resref      8)
   :flags                      (ie/dword       4)
   :item-type                  (ie/word        2)
   :usability-bitmask          (ie/dword       4)
   :item-animation             (ie/_char-array 2)
   :min-level                  (ie/word        2)
   :min-strength               (ie/word        2)
   :min-strength-bonus         (ie/_byte       1)
   :kit-usability-1            (ie/_byte       1)
   :min-intelligence           (ie/_byte       1)
   :kit-usability-2            (ie/_byte       1)
   :min-dexterity              (ie/_byte       1)
   :kit-usability-3            (ie/_byte       1)
   :min-wisdom                 (ie/_byte       1)
   :kit-usability-4            (ie/_byte       1)
   :min-constitution           (ie/_byte       1)
   :weapon-proficiency         (ie/_byte       1)
   :min-charisma               (ie/word        2)
   :price                      (ie/dword       4)
   :stack-amount               (ie/word        2)
   :inventory-icon             (ie/resref      8)
   :lore-to-id                 (ie/word        2)
   :ground-icon                (ie/resref      8)
   :weight                     (ie/dword       4)
   :unidentified-description   (ie/strref      4)
   :identified-description     (ie/strref      4)
   :description-icon           (ie/resref      8)
   :enchantment                (ie/dword       4)
   :offset-to-extended-headers (ie/dword       4)
   :count-of-extended-headers  (ie/word        2)
   :offset-to-feature-blocks   (ie/dword       4)
   :index-into-feature-blocks  (ie/word        2)
   :count-of-feature-blocks    (ie/word        2)
   ])

(def ext-header-spec
  [
   :attack-type               (ie/_char  1)
   :id-req                    (ie/_char  1)
   :location                  (ie/_char  1)
   :alternative-dice-sides    (ie/_char  1)
   :use-icon                  (ie/resref 8)
   :target-type               (ie/_char  1)
   :target-count              (ie/_char  1)
   :range                     (ie/word   2)
   :launcher-required         (ie/_byte  1)
   :alternative-dice-thrown   (ie/_byte  1)
   :speed-factor              (ie/_byte  1)
   :alternative-damage-bonus  (ie/_byte  1)
   :thac0-bonus               (ie/word   2)
   :dice-sides                (ie/_byte  1)
   :primary-type-school       (ie/_byte  1)
   :dice-thrown               (ie/_byte  1)
   :secondary-type            (ie/_byte  1)
   :damage-bonus              (ie/word   2)
   :damage-type               (ie/word   2)
   :count-of-feature-blocks   (ie/word   2)
   :index-into-feature-blocks (ie/word   2)
   :max-charges               (ie/word   2)
   :charge-depletion-behavior (ie/word   2)
   :flags                     (ie/dword  4)
   :projectile-animation      (ie/word   2)
   :melee-animation-1         (ie/word   2)
   :melee-animation-2         (ie/word   2)
   :melee-animation-3         (ie/word   2)
   :is-arrow?                 (ie/word   2)
   :is-bolt?                  (ie/word   2)
   :is-bullet?                (ie/word   2)
   ])

(def feature-block-spec
  [
   :opcode-number         (ie/word   2)
   :target-type           (ie/_char  1)
   :power                 (ie/_char  1)
   :parameter-1           (ie/sdword 4)
   :parameter-2           (ie/sdword 4)
   :timing-mode           (ie/_char  1)
   :dispel-resistance     (ie/_char  1)
   :duration              (ie/dword  4)
   :probability-1         (ie/_char  1)
   :probability-2         (ie/_char  1)
   :resource              (ie/resref 8)
   :dice-thrown-max-level (ie/dword  4)
   :dice-sides-min-level  (ie/dword  4)
   :saving-throw-type     (ie/dword  4)
   :saving-throw-bonus    (ie/dword  4)
   :tobex-stacking-id     (ie/dword  4)
   ])

;; NOTE: strref = int reference in dialog.tlk, resref = 8 byte ascii w/ garbage
;; char array = X byte ascii, everything else = little endian values
(def header-frame (apply c/ordered-map header-spec))
(def ext-header-frame (apply c/ordered-map ext-header-spec))
(def feature-block-frame (apply c/ordered-map feature-block-spec))

(def itm-frame
  (c/header
   header-frame
   (fn [y]
     (c/repeated ext-header-frame :prefix :none))
   (fn [x] x)
   ))

;; The BG files need the ISO-8859-1 encoding - ascii and utf-8 didn't work out...
(defn get-item-x [s]
  (let [content (slurp (str "/home/mcarter/bgee/bgee2/override/" s) :encoding "ISO8859-1")]
    (->
     (java.nio.ByteBuffer/wrap (.getBytes content java.nio.charset.StandardCharsets/ISO_8859_1))
     (.order java.nio.ByteOrder/LITTLE_ENDIAN))))

(defn get-item [s]
  (with-open
    [in-stream (clojure.java.io/input-stream
                (str "/home/mcarter/bgee/bgee2/override/" s))]
    (->
     (java.nio.ByteBuffer/wrap (.readAllBytes in-stream))
     (.order java.nio.ByteOrder/LITTLE_ENDIAN))))

(defn get-proper-byte []
  (assert (= 0xAC (.get (.slice (get-item "qdmfist.itm") 8 1) 0))))

;; I think multiple iteration/offsets may be required
;; Or we'll have to go beyond the tool and parse programmatically
(defn test-item-header [s]
  (let [bytes (get-item s)]
    {:headers (io/decode header-frame (.slice bytes 0 0x72))
     :ext-headers (io/decode itm-frame (.slice bytes 0 (+ 0x72 56)))}))

(defn ext-headers-slice-bytes [bytes header iter]
  (.slice bytes (+ (:offset-to-extended-headers header) (* iter 56)) 56))

(defn feature-blocks-slice-bytes [bytes header iter]
  (.slice bytes (+ (:offset-to-feature-blocks header) (* iter 48)) 48))

;; FIXME: Failing on sw1h03.itm - WHY?
(defn parse-item [s]
  (let [bytes (get-item s)]
    (let [header (io/decode header-frame (.slice bytes 0 0x72))]
      {:header header

       :ext-headers
       (map (fn [i] (io/decode ext-header-frame (ext-headers-slice-bytes bytes header i)))
            (range (:count-of-extended-headers header)))

       :feature-blocks
       (map (fn [i] (io/decode feature-block-frame (feature-blocks-slice-bytes bytes header i)))
            (range (:count-of-feature-blocks header)))

       })
    )
  )

;; Due to how we are parsing, we need to do 3 types of encoding
;; Generate the header (TODO: Keep sizing synced to ext-headers/feature-blocks)
;; Encode each ext-header and then merge all the byte buffers
;; Encode each feature-blocks and then merge all the byte buffers
(defn parsed->bytes [parsed]
  {
   :header (io/encode header-frame (:header parsed))
   :ext-headers (map (fn [p] (io/encode ext-header-frame p)) (:ext-headers parsed))
   :feature-blocks (map (fn [p] (io/encode feature-block-frame p)) (:feature-blocks parsed))
   })

(defn parsed->flatbytes [parsed]
  (let [bytes (parsed->bytes parsed)]
    (flatten [(:header bytes) (:ext-headers bytes) (:feature-blocks bytes)])))

(defn get-allocation-size [byte-buffers]
  (reduce (fn [acc bb] (+ acc (.limit bb))) 0 byte-buffers))

;; FIXME: This is *almost* working - either the codec is out of order, or the endian-ness needs
;; some special care
(defn parsed->file [filename parsed]
  (with-open
    [out-stream (clojure.java.io/output-stream filename :encoding "ASCII")
     channel (java.nio.channels.Channels/newChannel out-stream)]
    (let [byte-buffers (parsed->flatbytes parsed)]
      (doall (map (fn [byte-buffer]
                    (.write channel byte-buffer)) byte-buffers))
      true)))

(defn batch-import
  "Run a bunch of inserts with special optimizations for sqlite3 db."
  [rows]
  ;; Insert all the top level data rows
  (j/with-db-transaction [t-con db/db]

    (doall (pvalues
            (j/insert-multi! t-con "itm_header"
                             (db/fast-rows-normalizer (map :header rows)))

            ;; Now insert all the related data rows
            ;; (j/insert-multi! t-con "itm_ext_header"
            ;;                  (db/fast-rows-normalizer (flatten (map :ext-headers rows))))

            (doall (pmap (fn [row-set]
                           (j/insert-multi! t-con "itm_ext_header" (db/fast-rows-normalizer row-set))
                           ) (map :ext-headers rows)))

            ;; (j/insert-multi! t-con "itm_feature_block"
            ;;                  (db/fast-rows-normalizer (flatten (map :feature-blocks rows))))

            (doall (pmap (fn [row-set]
                           (j/insert-multi! t-con "itm_feature_block" (db/fast-rows-normalizer row-set))
                           ) (map :feature-blocks rows)))))

    ))

(defn batch-csv
  "Write a bunch of rows to a csv."
  [rows]
  ;; Insert all the top level data rows
  (doall
   (pvalues
    (util/rows->csv "itm_header" (db/fast-rows-normalizer (map :header rows)))
    (util/rows->csv "itm_ext_header" (db/fast-rows-normalizer (flatten (map :ext-headers rows))))
    (util/rows->csv "itm_feature_block" (db/fast-rows-normalizer (flatten (map :feature-blocks rows))))
    )))

(defn index-itm [override-dir]
  (db/make-table-from-spec db/db "itm_header" header-spec "text" "unique")
  (db/make-table-from-spec db/db "itm_ext_header" ext-header-spec "text" "")
  (db/make-table-from-spec db/db "itm_feature_block" feature-block-spec "text" "")
  (j/delete! db/db "itm_header" ["1 = 1"])
  (j/delete! db/db "itm_ext_header" ["1 = 1"])
  (j/delete! db/db "itm_feature_block" ["1 = 1"])
  (->> (util/glob-dir override-dir #".*\.itm$")
       (pmap (fn [name]
               ;; Add the filename to each resource we speced out
               (let [parsed (parse-item name)]
                 {:header (conj (:header parsed) {:pkid name})
                  :ext-headers (map (fn [x] (conj x {:pkid name})) (:ext-headers parsed))
                  :feature-blocks (map (fn [x] (conj x {:pkid name})) (:feature-blocks parsed))})))
       batch-import
       ;; batch-csv
       count))
