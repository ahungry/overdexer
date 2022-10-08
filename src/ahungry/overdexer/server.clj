(ns ahungry.overdexer.server
  (:require
   [clojure.data.json :as json]
   [clojure.pprint]
   [clojure.tools.logging :as log]
   [ring.adapter.jetty :as jetty]
   [ring.util.response :refer [redirect]]
   [ring.middleware.resource :refer [wrap-resource]]
   [compojure.core :as compojure]
   [compojure.route :as compojure-route]
   ))

(defn get-version []
  (Thread/sleep 1000)
  "0.0.2")

(defn get-items []
  [{:name "foo"} {:name "bar"}])

(defn get-landing-page []
  (redirect "/app.html"))

(compojure/defroutes api-routes
  (compojure/GET "/items.json" [] {:body (get-items)})
  (compojure/GET "/version.json" [] {:body {:version (get-version)}}))

(compojure/defroutes web-routes
  (compojure/GET "/" [] (get-landing-page))
  (compojure-route/not-found "Page not found"))

(defn wrap-cors [handler]
  (fn [req]
    (let [res (handler req)]
      (-> res
          (assoc-in [:headers "Access-Control-Allow-Credentials"] "true")
          (assoc-in [:headers "Access-Control-Allow-Methods"] "GET,HEAD,OPTIONS,POST,PUT,PATCH")
          (assoc-in [:headers "Access-Control-Allow-Headers"] "Access-Control-Allow-Headers, Authorization, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers")
          (assoc-in [:headers "Access-Control-Allow-Origin"] "*")))))

(defn wrap-headers [handler]
  (fn [req]
    (let [res (handler req)]
      (-> res (assoc-in [:headers "content-type"] "application/json")))))

(defn wrap-json [handler]
  (fn [req]
    (let [res (handler req)]
      (-> res (update-in [:body] json/write-str)))))

(defn wrap-log [handler]
  (fn [req]
    (log/trace req)
    (handler req)))

(def app
  (compojure/routes
   (-> api-routes
       (compojure/wrap-routes #'wrap-log)
       (compojure/wrap-routes #'wrap-cors)
       (compojure/wrap-routes #'wrap-headers)
       (compojure/wrap-routes #'wrap-json))
   (-> web-routes
       (compojure/wrap-routes #'wrap-log))))

(defonce server (atom nil))

(defn start
  [& args]
  (if @server
    (.start @server)
    (reset!
     server
     (jetty/run-jetty
      (wrap-resource app "public")
      {:port 3000
       :join? (or (first args) false)})))
  (.browse (java.awt.Desktop/getDesktop) (java.net.URI. "http://localhost:3000")))

(defn stop []
  (when @server (.stop @server)))

(defn reset []
  (stop)
  (reset! server nil)
  (start))

(def restart reset)
