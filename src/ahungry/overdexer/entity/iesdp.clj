(ns ahungry.overdexer.entity.iesdp
  (:require
   [gloss.core :as c]
   [gloss.io :as io]))

(defn char-array [n]
  (c/string :utf-8 :length n))

(defn strref [_] {:type :strref :val :int32-be})

(defn resref [n] (char-array n))

(defn _byte [_] :byte)

(defn word [_] :int16-le)

(defn dword [_] :int32-le)

(defn _char [_] :byte)
