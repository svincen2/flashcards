(ns flashcards.events.flashcards
  (:require-macros [cljs.core.async.macros :as am])
  (:require
   [re-frame.core :as re-frame]
   [cljs-http.client :as http]
   [cljs.core.async :as a]))

(def url "http://localhost:3000/api/flashcards")

(re-frame/reg-event-db
 ::on-fail
 (fn [db [_ event res]]
   (assoc-in db [:errors event] res)))

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
       (let [{:keys [status] :as res} (a/<! (http/post url req))]
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
       (let [{:keys [status] :as res} (a/<! (http/delete url req))]
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
       (let [{:keys [status body] :as res} (a/<! (http/get url req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-flashcards-success body])
           (re-frame/dispatch [::on-fail ::fetch-flashcards res]))))
     ;; Do nothing
     {})))
