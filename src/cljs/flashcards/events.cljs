(ns flashcards.events
  (:require-macros [cljs.core.async.macros :as am]
                   [taoensso.timbre :as log])
  (:require [re-frame.core :as re-frame]
            [cljs-http.client :as http]
            [cljs.core.async :as a]
            [flashcards.db :as db]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-tab
 (fn [db [_ active-tab]]
   (assoc db :active-tab active-tab)))

(def api-url "http://localhost:3000/api/")

(re-frame/reg-event-db
 ::on-fail
 (fn [db [_ event res]]
   (assoc-in db [:errors event] res)))

;; cards

(re-frame/reg-event-db
 ::fetch-cards-success
 (fn [db [_ body]]
   (assoc db :cards (vec body))))

(re-frame/reg-event-fx
 ::create-card
 (fn [_ [_ question answer deck-ids]]
   (let [req {:edn-params {:question question :answer answer :deck-ids deck-ids}
              :with-credentials? false}]
     (am/go
       (let [{:keys [status] :as res} (a/<! (http/post (str api-url "cards") req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-cards])
           (re-frame/dispatch [::on-fail ::create-card res]))))
     ;; Do nothing
     {})))

(re-frame/reg-event-fx
 ::delete-card
 (fn [_ [_ id]]
   (let [req {:edn-params {:id id}
              :with-credentials? false}]
     (am/go
       (let [{:keys [status] :as res} (a/<! (http/delete (str api-url "cards") req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-cards])
           (re-frame/dispatch [::on-fail ::delete-card id res]))))
     ;; Do nothing
     {})))

(re-frame/reg-event-fx
 ::fetch-cards
 (fn [_ [_ _]]
   (let [req {:with-credentials? false}]
     (am/go
       (let [{:keys [status body] :as res} (a/<! (http/get (str api-url "cards") req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-cards-success body])
           (re-frame/dispatch [::on-fail ::fetch-cards res]))))
     ;; Do nothing
     {})))

;; Decks

(re-frame/reg-event-db
 ::fetch-decks-success
 (fn [db [_ body]]
   (assoc db :decks (vec body))))

(re-frame/reg-event-fx
 ::create-deck
 (fn [_ [_ label color]]
   (let [req {:edn-params {:label label :color color}
              :with-credentials? false}]
     (am/go
       (let [{:keys [status] :as res} (a/<! (http/post (str api-url "decks") req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-decks])
           (re-frame/dispatch [::on-fail ::create-deck res]))))
     ;; Do nothing
     {})))

(re-frame/reg-event-fx
 ::delete-deck
 (fn [_ [_ id]]
   (let [req {:edn-params {:id id}
              :with-credentials? false}]
     (am/go
       (let [{:keys [status] :as res} (a/<! (http/delete (str api-url "decks") req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-decks])
           (re-frame/dispatch [::on-fail ::delete-deck id res]))))
     ;; Do nothing
     {})))

(re-frame/reg-event-fx
 ::fetch-decks
 (fn [_ [_ _]]
   (let [req {:with-credentials? false}]
     (am/go
       (let [{:keys [status body] :as res} (a/<! (http/get (str api-url "decks") req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-decks-success body])
           (re-frame/dispatch [::on-fail ::fetch-decks res]))))
     ;; Do nothing
     {})))
