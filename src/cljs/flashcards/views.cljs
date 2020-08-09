(ns flashcards.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [re-com.core :as re-com]
   [flashcards.events :as events]
   [flashcards.subs :as subs]
   [taoensso.timbre :as log]
   ))

;; create flashcard
(defn create-flashcard-component
  []
  (let [question (r/atom nil)
        answer (r/atom nil)]
    (fn []
      [re-com/v-box
       :gap "1em"
       :padding "8px"
       :children [[re-com/label :label "Question"]
                  [re-com/input-text
                   :model question
                   :on-change #(reset! question %)]
                  [re-com/label :label "Answer"]
                  [re-com/input-text
                   :model answer
                   :on-change #(reset! answer %)]
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

;; home

(defn home-title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :label @name
     :level :level1]))

(defn home-panel []
  [re-com/v-box
   :gap "1em"
   :padding "8px"
   :children [[home-title]
              [create-flashcard-component]
              [flashcards-list-component]]])

;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    [:div "Something went horribly wrong..."]))

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :height "100%"
     :padding "8px"
     :children [[panels @active-panel]]]))
