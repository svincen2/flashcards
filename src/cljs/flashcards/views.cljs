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
                          :height "250px"
                          :width "100%"
                          :style {:border "1px solid green"
                                  :border-radius "0.5em"}
                          :justify :between
                          :children [[re-com/box
                                      :width "100%"
                                      :padding "8px"
                                      :align :center
                                      :child [re-com/title
                                              :level :level2
                                              :label (:question card)
                                              :style {:align-text :center}]]
                                     [re-com/box
                                      :width "100%"
                                      :padding "8px"
                                      :align :center
                                      :child [re-com/title
                                              :level :level3
                                              :style {:color "gray"
                                                      :cursor (if @revealed? "default" "pointer")
                                                      }
                                              :label (if @revealed? (:answer card) "Check answer")
                                              :attr {:on-click #(reset! revealed? true)}]]]])
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
  [re-com/v-box
   :gap "1em"
   :padding "8px"
   :style {:border "1px solid green"
           :border-radius "0.5em"}
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
                          [re-com/label :label (:answer flashcard)]]]]])

(defn cards-panel
  []
  (let [flashcards (re-frame/subscribe [::subs/flashcards])]
    [re-com/v-box
     :gap "1em"
     :width "100%"
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
  [re-com/v-box
   :gap "1em"
   :padding "8px"
   :style {:border "1px solid green"
           :border-radius "0.5em"}
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
               :child [re-com/label :label (:description deck)]]]])

(defn decks-panel
  []
  (let [decks (re-frame/subscribe [::subs/decks])]
    [re-com/v-box
     :gap "1em"
     :width "100%"
     :children (for [deck @decks]
                 ^{:key (:id deck)}
                 [deck-item deck])]))

;; main

(defn main-panel []
  (re-frame/dispatch [::events/fetch-flashcards])
  (re-frame/dispatch [::events/fetch-decks])
  (let [active-tab (re-frame/subscribe [::subs/active-tab])]
    [re-com/v-box
     :height "100%"
     :align :center
     :children [[re-com/v-box
                 :gap "1em"
                 :width "650px"
                 :children [[re-com/box
                             :align :center
                             :child [re-com/title
                                     :label "Flashcards"
                                     :level :level1]]
                            [re-com/horizontal-tabs
                             :model active-tab
                             :style {:border-radius "0.5em 0.5em 0 0"}
                             :tabs [{:id :cards :label "Cards"}
                                    {:id :decks :label "Decks"}
                                    {:id :quiz :label "Quiz"}]
                             :on-change #(re-frame/dispatch [::events/set-active-tab %])]
                            (case @active-tab
                              :cards [cards-panel]
                              :decks [decks-panel]
                              :quiz [quiz-panel])]]]]))
