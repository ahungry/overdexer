(ns ahungry.overdexer.resource.itm
  (:require
   [clojure.java.io]
   [gloss.core :as c]
   [gloss.io :as io]))

;; https://gibberlings3.github.io/iesdp/file_formats/ie_formats/itm_v1.htm

;; https://github.com/clj-commons/gloss/wiki/Introduction
;; https://cljdoc.org/d/org.clj-commons/gloss/0.3.0/api/gloss
;; https://github.com/d-t-w/by-example-gloss/blob/master/src/by_example_gloss/core.clj

(def sig-frame (c/compile-frame {:name (c/string :utf-8 :length 4)}))

(defn get-item [s]
  (slurp (str "/home/mcarter/bgee/bgee2/override/" s)))

(defn foo []
  (prn "Yay")
  io/decode)

(defn d []
  (io/decode sig-frame (get-item "qdmfist.itm")))

(defn e []
  (io/encode sig-frame {:name "matt"}))

(defn out-to-file []
  (let [records [{:name "matt"}]]
    (with-open
      [out-stream (clojure.java.io/output-stream "file:///tmp/x.log")]
      (io/encode-to-stream sig-frame out-stream records))))
