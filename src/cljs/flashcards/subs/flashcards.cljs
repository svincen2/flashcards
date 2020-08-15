(ns flashcards.subs.flashcards
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::flashcards
 (fn [db _]
   (:flashcards db)))
