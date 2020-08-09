(ns flashcards.events
  (:require-macros [cljs.core.async.macros :as am]
                   [taoensso.timbre :as log])
  (:require
   [re-frame.core :as re-frame]
   [flashcards.db :as db]
   [cljs-http.client :as http]
   [cljs.core.async :as a]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-db
 ::create-flashcard-fail
 (fn [db [_ {:keys [body] :as res}]]
   (log/info "create-flashcard-success" body)
   (assoc-in db [:errors ::create-flashcard] res)))

(re-frame/reg-event-db
 ::fetch-flashcards-success
 (fn [db [_ body]]
   (log/info "fetch-flashcards-success" body)
   (assoc db :flashcards (vec body))))

(re-frame/reg-event-db
 ::fetch-flashcards-fail
 (fn [db [_ res]]
   (log/info "fetch-flashcards-fail" res)
   (assoc-in db [:errors ::fetch-flashcards] res)))

(re-frame/reg-event-db
 ::delete-flashcard-fail
 (fn [db [_ id res]]
   (log/info "delete-flashcards-fail" id res)
   (assoc-in db [:errors ::delete-flashcard-fail id] res)))

(re-frame/reg-event-fx
 ::create-flashcard
 (fn [_ [_ question answer]]
   (let [url "http://localhost:3000/api/flashcards"
         req {:edn-params {:question question :answer answer}
              :with-credentials? false}]
     (am/go
       (let [{:keys [status] :as res} (a/<! (http/post url req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-flashcards])
           (re-frame/dispatch [::create-flashcard-fail res]))))
     ;; Do nothing
     {})))

(re-frame/reg-event-fx
 ::delete-flashcard
 (fn [_ [_ id]]
   (let [url "http://localhost:3000/api/flashcards"
         req {:edn-params {:id id}
              :with-credentials? false}]
     (am/go
       (let [{:keys [status] :as res} (a/<! (http/delete url req))]
         (log/info "delete-flashcard" status)
         (if (= 200 status)
           (re-frame/dispatch [::fetch-flashcards])
           (re-frame/dispatch [::delete-flashcard-fail id res]))))
     ;; Do nothing
     {})))

(re-frame/reg-event-fx
 ::fetch-flashcards
 (fn [_ [_ _]]
   (log/info "fetch-flashcards")
   (let [url "http://localhost:3000/api/flashcards"
         req {:with-credentials? false}]
     (am/go
       (let [{:keys [status body] :as res} (a/<! (http/get url req))]
         (if (= 200 status)
           (re-frame/dispatch [::fetch-flashcards-success body])
           (re-frame/dispatch [::fetch-flashcards-fail res]))))
     ;; Do nothing
     {})))
