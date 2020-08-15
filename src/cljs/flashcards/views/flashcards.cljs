(ns flashcards.views.flashcards
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [re-com.core :as re-com]
   [flashcards.events.flashcards :as events]
   [flashcards.subs.flashcards :as subs]
   [taoensso.timbre :as log]))

(defn create-flashcard-component
  []
  (let [question (r/atom nil)
        answer (r/atom nil)]
    (fn []
      [re-com/v-box
       :gap "1em"
       :padding "8px"
       :children [[re-com/h-box
                   :gap "1em"
                   :children [[re-com/label :label "Q:"]
                              [re-com/input-text
                               :model question
                               :on-change #(reset! question %)]]]
                  [re-com/h-box
                   :gap "1em"
                   :children [[re-com/label :label "A:"]
                              [re-com/input-text
                               :model answer
                               :on-change #(reset! answer %)]]]
                  [re-com/button
                   :label "Create"
                   :on-click #(let [q @question
                                    a @answer]
                                (re-frame/dispatch [::events/create-flashcard q a])
                                (reset! question nil)
                                (reset! answer nil))]]])))

(defn flashcard-list-item
  [flashcard]
  [re-com/border
   :border "1px dashed green"
   :radius "1em"
   :child [re-com/v-box
           :gap "1em"
           :padding "8px"
           :children [[re-com/h-box
                       :justify :between
                       :children [[re-com/h-box
                                   :gap "1em"
                                   :padding "8px"
                                   :children [[re-com/label :label "Q:"]
                                              [re-com/label :label (:question flashcard)]]]
                                  [re-com/h-box
                                   :gap "1em"
                                   :padding "8px"
                                   :children [[re-com/md-icon-button
                                               :md-icon-name "zmdi-delete"
                                               :style {:float :right}
                                               :on-click #(re-frame/dispatch [::events/delete-flashcard (:id flashcard)])]]]]]
                      [re-com/line]
                      [re-com/h-box
                       :gap "1em"
                       :padding "8px"
                       :children [[re-com/label :label "A:"]
                                  [re-com/label :label (:answer flashcard)]]]]]])

(defn flashcards-list-component
  []
  (re-frame/dispatch [::events/fetch-flashcards])
  (let [flashcards (re-frame/subscribe [::subs/flashcards])]
    [re-com/v-box
     :gap "1em"
     :padding "8px"
     :children (for [flashcard @flashcards]
                 ^{:key (:id flashcard)}
                 [flashcard-list-item flashcard])]))