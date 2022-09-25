(ns ahungry.overdexer.entity.iesdp
  (:require
   [gloss.core :as c]
   [gloss.io :as io]))

(defn _char-array [n]
  (c/string :utf-8 :length n))

(defn strref [_] {:type :strref :val :uint32-be})

;; FIXME: SUPPOSEDLY this is a null terminated string
;; But quite a few records fail to parse it into readable values...
(defn resref [n] {:type :resref :val (repeat 8 :byte)})

(defn _byte [_] :byte)

(defn word [_] :uint16-le)

(defn dword [_] :uint32-le)

(defn _char [_] :byte)

(defn safe-string [n] {:type :resref :val (repeat n :byte)})
