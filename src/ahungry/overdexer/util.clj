(ns ahungry.overdexer.util)

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
