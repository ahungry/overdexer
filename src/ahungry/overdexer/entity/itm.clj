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
   :attack-type               (ie/char-array 1)
   :id-req                    (ie/char-array 1)
   :location                  (ie/char-array 1)
   :alternative-dice-sides    (ie/char-array 1)
   :use-icon                  (ie/resref     8)
   :target-type               (ie/char-array 1)
   :target-count              (ie/char-array 1)
   :range                     (ie/word       2)
   :launcher-required         (ie/_byte      1)
   :alternative-dice-thrown   (ie/_byte      1)
   :speed-factor              (ie/_byte      1)
   :alternative-damage-bonus  (ie/_byte      1)
   :thac0-bonus               (ie/word       2)
   :dice-sides                (ie/_byte      1)
   :primary-type-school       (ie/_byte      1)
   :dice-thrown               (ie/_byte      1)
   :secondary-type            (ie/_byte      1)
   :damage-bonus              (ie/word       2)
   :damage-type               (ie/word       2)
   :count-of-feature-blocks   (ie/word       2)
   :index-into-feature-blocks (ie/word       2)
   :max-charges               (ie/word       2)
   :charge-depletion-behavior (ie/word       2)
   :flags                     (ie/dword      4)
   :projectile-animation      (ie/word       2)
   :melee-animation-1         (ie/word       2)
   :melee-animation-2         (ie/word       2)
   :melee-animation-3         (ie/word       2)
   :is-arrow?                 (ie/word       2)
   :is-bolt?                  (ie/word       2)
   :is-bullet?                (ie/word       2)
   ])
;; NOTE: strref = int reference in dialog.tlk, resref = 8 byte ascii w/ garbage
;; char array = X byte ascii, everything else = little endian values
(def header-frame (apply c/ordered-map header-spec))


(def ext-header-frame
  (c/ordered-map
   :x :byte))

;; FIXME: Probably read directly into the java.nio.HeapByteBuffer this creates,
;; instead of slurping and then translating
(defn get-item [s]
  (let [content (slurp (str "/home/mcarter/bgee/bgee2/override/" s))]
    (->
     (java.nio.ByteBuffer/wrap (.getBytes content java.nio.charset.StandardCharsets/US_ASCII))
     (.order java.nio.ByteOrder/LITTLE_ENDIAN))))

(defn test-item-header [s]
  (try
    (io/decode header-frame (.slice (get-item s) 0 0x72))
    (catch Exception _ nil)))

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
