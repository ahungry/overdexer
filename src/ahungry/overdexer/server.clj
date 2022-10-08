(ns ahungry.overdexer.server
  (:require
   [clojure.data.json :as json]
   [clojure.pprint]
   [ring.adapter.jetty :as jetty]
   [compojure.core :as compojure]
   [compojure.route :as compojure-route]
   ))

(defn get-version [] "0.0.2")

(defn get-landing-page []
  "<b>Hello world</b>")

(compojure/defroutes api-routes
  (compojure/GET "/version.json" [] {:body {:version (get-version)}}))

(compojure/defroutes web-routes
  (compojure/GET "/" [] (get-landing-page))
  (compojure-route/not-found "Page not found"))


(defn wrap-headers [handler]
  (fn [req]
    (let [res (handler req)]
      (-> res (assoc-in [:headers "content-type"] "application/json")))))

(defn wrap-json [handler]
  (fn [req]
    (let [res (handler req)]
      (-> res (update-in [:body] json/write-str)))))

(def app
  (compojure/routes
   (-> api-routes
       (compojure/wrap-routes #'wrap-headers)
       (compojure/wrap-routes #'wrap-json))
   web-routes))

(defn handler [request]
  (clojure.pprint/pprint request)
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defonce server (atom nil))

(defn start
  [& args]
  (if @server
    (.start @server)
    (do
      (reset!
       server
       (jetty/run-jetty
        app
        {:port 3000
         :join? (or (first args) false)}))))
  (.browse (java.awt.Desktop/getDesktop) (java.net.URI. "http://localhost:3000")))

(defn stop []
  (when @server (.stop @server)))

(defn reset []
  (stop)
  (reset! server nil)
  (start))
