(ns ahungry.overdexer.entity.dialog
  (:require
   [ahungry.overdexer.entity.db :refer [db]]
   [ahungry.overdexer.entity.iesdp :as ie]
   [ahungry.overdexer.util :as util]
   [clojure.java.jdbc :as j]
   [clojure.java.io]
   [clojure.string]
   [clojure.walk]
   [gloss.core :as c]
   [gloss.io :as io]))

;; https://gibberlings3.github.io/iesdp/file_formats/ie_formats/tlk_v1.htm
(def header-spec
  [
   :signature                (ie/_char-array 4)
   :version                  (ie/_char-array 4)
   :language-id              (ie/word        2)
   :number-of-strref-entries (ie/dword       4)
   :offset-to-string-data    (ie/dword       4)
   ])

(def entry-spec
  [
   :bit-field       (ie/word   2)
   :sound           (ie/resref 8)
   :volume-variance (ie/dword  4)
   :pitch-variance  (ie/dword  4)
   :offset          (ie/dword  4)
   :length          (ie/dword  4)
   ])
