(ns flashcards.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [re-com.core :as re-com]
   [components :as comps]
   [flashcards.events :as events]
   [flashcards.subs :as subs]
   [taoensso.timbre :as log]))

;; Quizzes


(defn quiz-panel
  []
  (let [quiz-cards (shuffle @(re-frame/subscribe [::subs/flashcards]))
        revealed? (r/atom false)]
    (fn []
      [comps/paginated-panels-component
       :on-click #(reset! revealed? false)
       :children (mapv (fn [card]
                         [re-com/v-box
                          :gap "1em"
                          :padding "8px"
                          :align :center
                          :children [[re-com/title
                                      :level :level2
                                      :label (:question card)]
                                     [re-com/title
                                      :level :level3
                                      :style {:color "gray"
                                              :cursor (if @revealed? "default" "pointer")}
                                      :label (if @revealed? (:answer card) "Check answer")
                                      :attr {:on-click #(reset! revealed? true)}]]])
                       quiz-cards)])))

;; Cards

(defn new-panel
  []
  (let [question (r/atom nil)
        answer (r/atom nil)
        deck (r/atom :default)
        decks (re-frame/subscribe [::subs/decks])]
    (fn []
      [re-com/v-box
       :gap "1em"
       :padding "8px"
       :children [[re-com/h-box
                   :gap "1em"
                   :justify :between
                   :children [[re-com/v-box
                               :gap "1em"
                               :children [[re-com/label :label "Question"]
                                          [re-com/input-textarea
                                           :model question
                                           :on-change #(reset! question %)]
                                          [re-com/label :label "Answer"]
                                          [re-com/input-textarea
                                           :model answer
                                           :on-change #(reset! answer %)]]]
                              [re-com/v-box
                               :gap "1em"
                               :align :end
                               :justify :start
                               :width "250px"
                               :children [[re-com/label :label "Add to deck"]
                                          [re-com/single-dropdown
                                           :choices (into [{:id :default :label "Default"}]
                                                          (map (fn [{:keys [id label]}]
                                                                 {:id id :label label})
                                                               @decks))
                                           :model deck
                                           :on-change #(reset! deck %)
                                           :width "100%"]]]]]
                  [re-com/button
                   :label "Create"
                   :on-click #(let [q @question
                                    a @answer]
                                (re-frame/dispatch [::events/create-flashcard q a])
                                (reset! question nil)
                                (reset! answer nil))]]])))

(defn cards-item
  [flashcard]
  [re-com/border
   :border "1px solid green"
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

(defn cards-panel
  []
  (let [flashcards (re-frame/subscribe [::subs/flashcards])]
    [re-com/v-box
     :gap "1em"
     :padding "8px"
     :children (into
                [[comps/collapsible-panel
                  :label "New"
                  :gap "1em"
                  :child [new-panel]]]
                (for [flashcard @flashcards]
                  ^{:key (:id flashcard)}
                  [cards-item flashcard]))]))

;; Decks

(defn deck-item
  [deck]
  [re-com/border
   :border "1px solid green"
   :radius "1em"
   :child [re-com/v-box
           :gap "1em"
           :padding "8px"
           :children [[re-com/h-box
                       :justify :between
                       :children [[re-com/box
                                   :padding "8px"
                                   :child [re-com/label :label (:label deck)]]
                                  [re-com/h-box
                                   :gap "1em"
                                   :padding "8px"
                                   :children [[re-com/md-icon-button
                                               :md-icon-name "zmdi-delete"
                                               :style {:float :right}
                                               :on-click #(re-frame/dispatch [::events/delete-deck (:id deck)])]]]]]
                      [re-com/line]
                      [re-com/box
                       :padding "8px"
                       :child [re-com/label :label (:description deck)]]]]])

(defn decks-panel
  []
  (let [decks (re-frame/subscribe [::subs/decks])]
    [re-com/v-box
     :gap "1em"
     :padding "8px"
     :children (for [deck @decks]
                 ^{:key (:id deck)}
                 [deck-item deck])]))

(defn flashcards-panel
  []
  (re-frame/dispatch [::events/fetch-flashcards])
  (re-frame/dispatch [::events/fetch-decks])
  (let [quiz? (r/atom false)]
    (fn []
      [re-com/v-box
       :gap "1em"
       :padding "8px"
       :width "650px"
       :children [[re-com/box
                   :align :center
                   :child [re-com/title
                           :label "Flashcards"
                           :level :level1]]
                  [comps/collapsible-panel
                   :label "Quiz"
                   :gap "1em"
                   :padding "8px"
                   :on-click #(swap! quiz? not)
                   :child [quiz-panel]]
                  [comps/collapsible-panel
                   :label "Cards"
                   :gap "1em"
                   :padding "8px"
                   :disabled? @quiz?
                   :child [cards-panel]]
                  [comps/collapsible-panel
                   :label "Decks"
                   :gap "1em"
                   :padding "8px"
                   :disabled? @quiz?
                   :child [decks-panel]]]])))

;; main

(defn main-panel []
  [re-com/v-box
   :height "100%"
   :padding "8px"
   :align :center
   :children [[flashcards-panel]]])
