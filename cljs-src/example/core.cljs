(ns example.core
  (:require
   [cljs.core.async :refer [<!]]
   [cljs-http.client :as http]
   [re-frame.core :as rf]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   )
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(prn "hello world!")
;; (js/alert "xa Hello")

;; Some sample stuff to illustrate how we can build a counter page
(rf/reg-event-db
 ::initialize-db
 (fn [_ _]
   {:model 0
    :version "???"}))

;; https://day8.github.io/re-frame/api-re-frame.core/
(rf/reg-event-db ::increment (fn [db _] (update db :model inc)))
(rf/reg-event-db ::get-version (fn [db _] (assoc db :version "1.2.3")))
(rf/reg-event-db ::set-version (fn [db [_ version]] (assoc db :version version)))
(rf/reg-event-db
 ::get-versionx
 (fn [db _]
   (go (let [res (<! (http/get "http://localhost:3000/version.json" {:with-credentials? false}))]
         (prn (:status res))
         (prn (:body res))
         (prn "Will the assoc ever work?")
         (prn (type res))
         (rf/dispatch [::set-version (get-in res [:body :version])])))
   db))

(rf/reg-sub ::model (fn [db _] (:model db)))
(rf/reg-sub ::version (fn [db _] (:version db)))

(rf/dispatch [::initialize-db])

(defn main []
  [:div "It worked"
   [:button {:on-click #(rf/dispatch [::increment])} "Click me"]
   [:button {:on-click #(rf/dispatch [::get-versionx])} "xGet Version!"]
   [:hr {:class "break"}]
   [:div "Counter: " @(rf/subscribe [::model])]
   [:div "Version: "  @(rf/subscribe [::version])]
   ])

(rdom/render [main] (.getElementById js/document "app"))
;; (def root (r/create-root (.getElementById js/document "app")))
;; (. root render main)
