(ns ahungry.overdexer.server
  (:require
   [clojure.pprint]
   [ring.adapter.jetty :as jetty]
   ))

(defn handler [request]
  (clojure.pprint/pprint request)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn start
  [& args]
  (jetty/run-jetty
   handler
   {:port 3000
    :join? true}))
