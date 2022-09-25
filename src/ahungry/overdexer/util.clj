(ns ahungry.overdexer.util
  (:require
   [clojure.java.io]))

;; https://www.geeksforgeeks.org/bit-manipulation-swap-endianness-of-a-number/
;; 5376 -> 21 // 0x1500 -> 0x0015
;; Shift left:  <-- // 0b01 0b010 // 1 2
(defn flip-endianess [n]
  (let [leftmost (bit-shift-right (bit-and n 0x000000FF) 0)
        leftmid (bit-shift-right (bit-and n 0x0000FF00) 8)
        rightmid (bit-shift-right (bit-and n 0x00FF0000) 16)
        rightmost (bit-shift-right (bit-and n 0xFF000000) 24)
        ]
    (bit-or
     (bit-shift-left leftmost 24)
     (bit-shift-left leftmid 16)
     (bit-shift-left rightmid 8)
     (bit-shift-left rightmost 0))))

(defn bytes->bb [bytes]
  (let [bytelen (count bytes)
        bb (java.nio.ByteBuffer/allocate bytelen)]
    (doall (map (fn [byt] (.put bb (byte byt))) bytes))
    bb))

(defn bytes->string
  "Get the array of numbers (bytes) and turn into a string."
  [bytes]
  (try
    (let [bb (bytes->bb bytes)]
      (String. (.array bb)))
    (catch Exception _ "")))

;; Some of these have values out of range or screw it up in other ways...
(defn get-resref [bytes]
  (if (vector? bytes)
    (let [nul (.indexOf bytes 0)]
      (bytes->string (if (> nul -1) (subvec bytes 0 nul) bytes)))
    ""))

(defn byte-array->endian [endian bytes]
  (let [bytelen (count bytes)
        bb (bytes->bb bytes)]
    (.order bb endian)
    (cond
      (>= bytelen 8) (.getLong bb 0)
      (>= bytelen 4) (.getInt bb 0)
      (>= bytelen 2) (.getShort bb 0)
      :else (.getByte bb 0))))

(def byte-array->le (partial byte-array->endian java.nio.ByteOrder/LITTLE_ENDIAN))
(def byte-array->be (partial byte-array->endian java.nio.ByteOrder/BIG_ENDIAN))

(defn glob-dir [dir rx]
  (let [directory (clojure.java.io/file dir)
        files (file-seq directory)]
    (->> files
         (map #(.getName %))
         (filter #(re-matches rx %)))))

(def glob (partial glob-dir "/home/mcarter/bgee/bgee2/override/"))
