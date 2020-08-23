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
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(def api-url "http://localhost:3000/api/")

(re-frame/reg-event-db
 ::on-fail
 (fn [db [_ event res]]
   (assoc-in db [:errors event] res)))

;; Flashcards

(re-frame/reg-event-db
 ::fetch-flashcards-success
 (fn [db [_ body]]
   (assoc db :flashcards (vec body))))

(re-frame/reg-event-fx
 ::create-flashcard
 (fn [_ [_ question answer]]
   (let [req {:edn-params {:question question :answer answer}
              :with-credentials? false}]
     (am/go
       (let [{:keys [status] :as res} (a/<! (http/post (str api-url "flashcards") req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-flashcards])
           (re-frame/dispatch [::on-fail ::create-flashcard res]))))
     ;; Do nothing
     {})))

(re-frame/reg-event-fx
 ::delete-flashcard
 (fn [_ [_ id]]
   (let [req {:edn-params {:id id}
              :with-credentials? false}]
     (am/go
       (let [{:keys [status] :as res} (a/<! (http/delete (str api-url "flashcards") req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-flashcards])
           (re-frame/dispatch [::on-fail ::delete-flashcard id res]))))
     ;; Do nothing
     {})))

(re-frame/reg-event-fx
 ::fetch-flashcards
 (fn [_ [_ _]]
   (let [req {:with-credentials? false}]
     (am/go
       (let [{:keys [status body] :as res} (a/<! (http/get (str api-url "flashcards") req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-flashcards-success body])
           (re-frame/dispatch [::on-fail ::fetch-flashcards res]))))
     ;; Do nothing
     {})))

;; Decks

(re-frame/reg-event-db
 ::fetch-decks-success
 (fn [db [_ body]]
   (assoc db :decks (vec body))))

(re-frame/reg-event-fx
 ::create-deck
 (fn [_ [_ label description]]
   (let [req {:edn-params {:label label :description description}
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
