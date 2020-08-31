(ns flashcards.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::cards
 (fn [db _]
   (:cards db)))

(re-frame/reg-sub
 ::decks
 (fn [db _]
   (:decks db)))

(re-frame/reg-sub
 ::active-tab
 (fn [db _]
   (:active-tab db)))
