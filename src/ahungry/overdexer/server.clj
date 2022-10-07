(ns ahungry.overdexer.server
  (:require
   [clojure.data.json :as json]
   [clojure.pprint]
   [ring.adapter.jetty :as jetty]
   [compojure.core :as compojure]
   [compojure.route :as compojure-route]
   ))

(compojure/defroutes my-routes
  (compojure/GET "/" [] "Hello World")
  (compojure/GET "/version" [] {:body {:version "0.0.1"}})
  (compojure-route/not-found "Page not found"))

(defn wrap-headers [handler]
  (fn [req]
    (prn "wrap-headers")
    (let [res (handler req)]
      (-> res (assoc-in [:headers "content-type"] "application/json")))))

(defn wrap-json [handler]
  (fn [req]
    (prn "wrap-json")
    (let [res (handler req)]
      (-> res (update-in [:body] json/write-str)))))

(def app
  (compojure/routes
   (-> my-routes
       (compojure/wrap-routes #'wrap-headers)
       (compojure/wrap-routes #'wrap-json))))

(defn handler [request]
  (clojure.pprint/pprint request)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn start
  [& args]
  (jetty/run-jetty
   app
   {:port 3000
    :join? true}))
