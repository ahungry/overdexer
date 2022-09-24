(ns ahungry.overdexer.entity.iesdp
  (:require
   [gloss.core :as c]
   [gloss.io :as io]))

(defn char-array [n]
  (c/string :utf-8 :length n))

(defn strref [_] {:type :strref :val [:byte :byte :byte :byte]})

(defn resref [n] (char-array n))

(defn _byte [_] :byte)

(defn word [_] [:byte :byte])

(defn dword [_] [:byte :byte :byte :byte])

(defn _char [_] :byte)
