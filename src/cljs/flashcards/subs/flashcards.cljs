(ns flashcards.subs.flashcards
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]))

(re-frame/reg-sub
 ::flashcards
 (fn [db _]
   (get-in db [:flashcards :flashcards])))
