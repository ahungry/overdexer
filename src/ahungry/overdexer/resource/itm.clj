(ns ahungry.overdexer.resource.itm
  (:require
   [clojure.java.io]
   [gloss.core :as c]
   [gloss.io :as io]))

;; https://gibberlings3.github.io/iesdp/file_formats/ie_formats/itm_v1.htm

;; https://github.com/clj-commons/gloss/wiki/Introduction
;; https://cljdoc.org/d/org.clj-commons/gloss/0.3.0/api/gloss
;; https://github.com/d-t-w/by-example-gloss/blob/master/src/by_example_gloss/core.clj

(def header-frame
  (c/ordered-map
   :signature (c/string :utf-8 :length 4)
   :version (c/string :utf-8 :length 4)
   :unidentified-name :int32
   :identified-name :int32
   :replacement-item :int64
   :flags :int32
   :item-type :int16
   :usability-bitmask :int32
   :item-animation :int16
   :min-level :int16
   :min-strength :int16
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
   :min-charisma :int16
   :price :int32
   :stack-amount :int16
   :inventory-icon :int64
   :lore-to-id :int16
   :ground-icon :int64
   :weight :int32
   :unidentified-description :int32
   :identified-description :int32
   :description-icon :int64
   :enchantment :int32
   :offset-to-extended-headers :int32
   :count-of-extended-headers :int16
   :offset-to-feature-blocks :int32
   :index-into-feature-blocks :int16
   :count-of-feature-blocks :int16
   ))

;; FIXME: Probably read directly into the java.nio.HeapByteBuffer this creates,
;; instead of slurping and then translating
(defn get-item [s]
  (let [content (slurp (str "/home/mcarter/bgee/bgee2/override/" s))]
    (java.nio.ByteBuffer/wrap (.getBytes content java.nio.charset.StandardCharsets/US_ASCII))))

(defn test-item-header []
  (io/decode header-frame (.slice (get-item "qdmfist.itm") 0 0x72)))

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
