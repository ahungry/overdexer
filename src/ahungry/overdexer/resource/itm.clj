(ns ahungry.overdexer.resource.itm
  (:require
   [clojure.java.io]
   [gloss.core :as c]
   [gloss.io :as io]))

;; https://gibberlings3.github.io/iesdp/file_formats/ie_formats/itm_v1.htm

;; https://github.com/clj-commons/gloss/wiki/Introduction
;; https://cljdoc.org/d/org.clj-commons/gloss/0.3.0/api/gloss
;; https://github.com/d-t-w/by-example-gloss/blob/master/src/by_example_gloss/core.clj

;; NOTE: strref = int reference in dialog.tlk, resref = 8 byte ascii w/ garbage
;; char array = X byte ascii, everything else = little endian values
(def header-frame
  (c/ordered-map
   :signature (c/string :utf-8 :length 4)
   :version (c/string :utf-8 :length 4)
   :unidentified-name :int32-le
   :identified-name :int32-le
   :replacement-item (c/string :utf-8 :length 8)
   :flags :int32-le
   :item-type :int16-le
   :usability-bitmask :int32-le
   :item-animation (c/string :utf-8 :length 2) ;; :int16-le
   :min-level :int16-le
   :min-strength :int16-le
   :min-strength-bonus :byte
   :kit-usability-1 :byte
   :min-intelligence :byte
   :kit-usability-2 :byte
   :min-dexterity :byte
   :kit-usability-3 :byte
   :min-wisdom :byte
   :kit-usability-4 :byte
   :min-constitution :byte
   :weapon-proficiency :byte
   :min-charisma :int16-le
   :price :int32-le
   :stack-amount :int16-le
   :inventory-icon (c/string :utf-8 :length 8)
   :lore-to-id :int16-le
   :ground-icon (c/string :utf-8 :length 8)
   :weight :int32-le
   :unidentified-description :int32-le
   :identified-description :int32-le
   :description-icon (c/string :utf-8 :length 8)
   :enchantment :int32-le
   :offset-to-extended-headers :int32-le
   :count-of-extended-headers :int16-le
   :offset-to-feature-blocks :int32-le
   :index-into-feature-blocks :int16-le
   :count-of-feature-blocks :int16-le))

;; FIXME: Probably read directly into the java.nio.HeapByteBuffer this creates,
;; instead of slurping and then translating
(defn get-item [s]
  (let [content (slurp (str "/home/mcarter/bgee/bgee2/override/" s))]
    (->
     (java.nio.ByteBuffer/wrap (.getBytes content java.nio.charset.StandardCharsets/US_ASCII))
     (.order java.nio.ByteOrder/LITTLE_ENDIAN))))

(defn test-item-header [s]
  (io/decode header-frame (.slice (get-item s) 0 0x72)))

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
