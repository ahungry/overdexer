(ns ahungry.overdexer.entity.itm
  (:require
   [ahungry.overdexer.entity.db :refer [db]]
   [ahungry.overdexer.entity.iesdp :as ie]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [gloss.core :as c]
   [gloss.io :as io]))

;; https://gibberlings3.github.io/iesdp/file_formats/general.htm
;; https://gibberlings3.github.io/iesdp/file_formats/ie_formats/itm_v1.htm

;; https://github.com/clj-commons/gloss/wiki/Introduction
;; https://cljdoc.org/d/org.clj-commons/gloss/0.3.0/api/gloss
;; https://github.com/d-t-w/by-example-gloss/blob/master/src/by_example_gloss/core.clj

(def header-spec
  [
   :signature                  (ie/char-array 4)
   :version                    (ie/char-array 4)
   :unidentified-name          (ie/strref     4)
   :identified-name            (ie/strref     4)
   :replacement-item           (ie/resref     8)
   :flags                      (ie/dword      4)
   :item-type                  (ie/word       2)
   :usability-bitmask          (ie/dword      4)
   :item-animation             (ie/char-array 2)
   :min-level                  (ie/word       2)
   :min-strength               (ie/word       2)
   :min-strength-bonus         (ie/_byte      1)
   :kit-usability-1            (ie/_byte      1)
   :min-intelligence           (ie/_byte      1)
   :kit-usability-2            (ie/_byte      1)
   :min-dexterity              (ie/_byte      1)
   :kit-usability-3            (ie/_byte      1)
   :min-wisdom                 (ie/_byte      1)
   :kit-usability-4            (ie/_byte      1)
   :min-constitution           (ie/_byte      1)
   :weapon-proficiency         (ie/_byte      1)
   :min-charisma               (ie/word       2)
   :price                      (ie/dword      4)
   :stack-amount               (ie/word       2)
   :inventory-icon             (ie/resref     8)
   :lore-to-id                 (ie/word       2)
   :ground-icon                (ie/resref     8)
   :weight                     (ie/dword      4)
   :unidentified-description   (ie/strref     4)
   :identified-description     (ie/strref     4)
   :description-icon           (ie/resref     8)
   :enchantment                (ie/dword      4)
   :offset-to-extended-headers (ie/dword      4)
   :count-of-extended-headers  (ie/word       2)
   :offset-to-feature-blocks   (ie/dword      4)
   :index-into-feature-blocks  (ie/word       2)
   :count-of-feature-blocks    (ie/word       2)
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
   :parameter-1           (ie/dword  4)
   :parameter-2           (ie/dword  4)
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

;; (c/defcodec itm-frame [header-frame (c/repeated ext-header-frame)])
(def itm-frame-x
  (c/ordered-map
   :header header-frame
   :ext-headers (c/repeated ext-header-frame :prefix :none)))

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

(defn byte-array->endian [endian bytes]
  (let [bytelen (count bytes)
        bb (java.nio.ByteBuffer/allocate bytelen)]
    (doall (map (fn [byt] (.put bb (byte byt))) bytes))
    (.order bb endian)
    (cond
      (>= bytelen 8) (.getLong bb 0)
      (>= bytelen 4) (.getInt bb 0)
      (>= bytelen 2) (.getShort bb 0)
      :else (.getByte bb 0))))

(def byte-array->le (partial byte-array->endian java.nio.ByteOrder/LITTLE_ENDIAN))
(def byte-array->be (partial byte-array->endian java.nio.ByteOrder/BIG_ENDIAN))

(defn ext-headers-slice-bytes [bytes header iter]
  (.slice bytes (+ (:offset-to-extended-headers header) (* iter 56)) 56))

(defn feature-blocks-slice-bytes [bytes header iter]
  (.slice bytes (+ (:offset-to-feature-blocks header) (* iter 48)) 48))

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

(defn test-ext-header [s]
  (let [bytes (get-item s)]
    (prn bytes)
    (io/decode ext-header-frame (.slice bytes 0x72 56))))

(defn foo []
  (prn "Yay"))

(defn d []
  (io/decode header-frame (get-item "qdmfist.itm")))

(defn e []
  (io/encode header-frame {:name "matt"}))

(defn out-to-file []
  (let [records [{:name "matt"}]]
    (with-open
      [out-stream (clojure.java.io/output-stream "file:///tmp/x.log")]
      (io/encode-to-stream header-frame out-stream records))))

(defn is-enchanted?
  "Check if enchantment is beyond some value."
  [file]
  (let [name (.getName file)
        item (test-item-header name)
        enchantment (:enchantment item)]
    (> (or enchantment 0) 6)))

(defn apply-enchantment-filters [file]
  (when (and (re-matches #".*\.itm$" (.getName file))
           (is-enchanted? file)) file))

(defn get-item-files
  "Pull out all items that match a given query condition"
  []
  (let [directory (clojure.java.io/file "/home/mcarter/bgee/bgee2/override")
        files (file-seq directory)]
    (->> files
         (pmap apply-enchantment-filters)
         (filter (complement nil?))
         (map #(.getName %)))))

(defn get-item-files-sequentially
  "Pull out all items that match a given query condition"
  []
  (let [directory (clojure.java.io/file "/home/mcarter/bgee/bgee2/override")
        files (file-seq directory)]
    (->> files
         (map apply-enchantment-filters)
         (filter (complement nil?))
         (map #(.getName %)))))

;; (j/query db ["select * from x"])

(defn make-table [db ]
  (j/execute! db (j/create-table-ddl "itm" [[:name "text"]] {:conditional? true})))

(j/query db ["select * from itm"])
