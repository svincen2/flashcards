(ns flashcards.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [re-com.core :as re-com]
   [components :as comps]
   [flashcards.events :as events]
   [flashcards.subs :as subs]
   [taoensso.timbre :as log]))

;; General

(defn deck-badge
  [{:keys [color label]}]
  [re-com/box
   :padding "4px"
   :style {:background-color (str "#" color)
           :border "1px solid gray"
           :border-radius "0.5em"}
   :child [re-com/label
           :label label]])

;; Quizzes

(defn quiz-panel
  []
  (let [quiz-cards (shuffle @(re-frame/subscribe [::subs/cards]))
        revealed? (r/atom false)]
    (fn []
      [comps/paginated-panels-component
       :on-click #(reset! revealed? false)
       :children (mapv (fn [card]
                         [re-com/v-box
                          :gap "1em"
                          :height "450px"
                          :width "100%"
                          :style {:border-bottom "1px solid lightgray"}
                          :justify :between
                          :children [[re-com/box
                                      :width "100%"
                                      :padding "8px"
                                      :align :center
                                      :child [re-com/title
                                              :level :level2
                                              :label (:question card)]]
                                     [re-com/box
                                      :width "100%"
                                      :padding "8px"
                                      :align :center
                                      :child [re-com/title
                                              :level :level2
                                              :style {:color "gray"
                                                      :cursor (if @revealed? "default" "pointer")}
                                              :label (if @revealed? (:answer card) "Check answer")
                                              :attr {:on-click #(reset! revealed? true)}]]]])
                       quiz-cards)])))

;; Cards

(defn new-card-panel
  []
  (let [question (r/atom nil)
        answer (r/atom nil)
        deck-ids (r/atom #{})
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
                               :children [[re-com/label :label "Add to decks"]
                                          [re-com/selection-list
                                           :choices (map (fn [{:keys [id label]}]
                                                           {:id id :label label})
                                                         @decks)
                                           :model deck-ids
                                           :on-change #(reset! deck-ids %)
                                           :width "100%"]]]]]
                  [re-com/button
                   :label "Create"
                   :disabled? (and (empty? @question) (empty? @answer))
                   :on-click #(let [q @question
                                    a @answer
                                    d (let [ids  @deck-ids] (if (seq ids) ids nil))]
                                (re-frame/dispatch [::events/create-card q a d])
                                (reset! question nil)
                                (reset! answer nil))]]])))

(defn cards-item
  [{:keys [id question answer deck-ids]} decks]
  [re-com/v-box
   :padding "8px"
   :style {:border-bottom "1px solid lightgray"}
   :children [[re-com/h-box
               :style {:border-bottom "1px dashed lightgray"
                       :padding-bottom "8px"
                       :padding-left "8px"}
               :justify :between
               :children [[re-com/h-box
                           :gap "1em"
                           :align :center
                           :width "400px"
                           :children [[re-com/label :label "Q:"]
                                      [:p
                                       {:style {:margin-bottom "0px"}}
                                       question]]]
                          [re-com/h-box
                           :gap "1em"
                           :align :center
                           :children (let [decks (->> deck-ids
                                                      (filter identity)
                                                      (map (partial get decks))
                                                      (sort-by :label))]
                                       (cond-> []
                                         (seq deck-ids) (into (mapv (fn [deck]
                                                                      [deck-badge deck])
                                                                    decks))
                                         :always (conj [re-com/box
                                                        :child [re-com/md-icon-button
                                                                :md-icon-name "zmdi-delete"
                                                                :style {:float :right}
                                                                :on-click #(re-frame/dispatch
                                                                            [::events/delete-card id])]])))]]]
              [re-com/h-box
               :gap "1em"
               :align :center
               :width "400px"
               :style {:padding-top "8px"
                       :padding-left "8px"}
               :children [[re-com/label :label "A:"]
                          [:p
                           {:style {:margin-bottom "0px"}}
                           answer]]]]])

(defn cards-panel
  []
  (let [cards (re-frame/subscribe [::subs/cards])
        decks (re-frame/subscribe [::subs/decks])]
    [re-com/v-box
     :width "100%"
     :children [[comps/collapsible-panel
                 :label "New"
                 :gap "1em"
                 :padding "8px"
                 :child [new-card-panel]]
                [re-com/scroller
                 :v-scroll :auto
                 :height "450px"
                 :child [re-com/v-box
                         :children (for [card @cards]
                                     ^{:key (:id card)}
                                     [cards-item
                                      card
                                      (into {} (map (fn [d] {(:id d) d}) @decks))])]]]]))

;; Decks

(def ^:private deck-label-re #"^([a-z]*|[a-z0-9\-]*)$")

(defn new-deck-panel
  []
  (let [label (r/atom nil)
        color (r/atom "ffffff")]
    (fn []
      [re-com/v-box
       :gap "1em"
       :padding "8px"
       :children [[re-com/h-box
                   :justify :between
                   :children [[re-com/v-box
                               :gap "1em"
                               :children [[re-com/label :label "Label"]
                                          [re-com/input-text
                                           :model label
                                           :validation-regex deck-label-re
                                           :change-on-blur? false
                                           :on-change #(reset! label %)]]]
                              [re-com/v-box
                               :gap "1em"
                               :align :end
                               :children [[re-com/label :label "Color"]
                                          [comps/color-picker color]]]]]
                  [re-com/button
                   :label "Create"
                   :disabled? (empty? @label)
                   :on-click #(do
                                (re-frame/dispatch [::events/create-deck @label @color])
                                (reset! label nil))]]])))

(defn deck-item
  [deck cards]
  [re-com/h-box
   :padding "8px"
   :style {:border-bottom "1px solid lightgray"}
   :justify :between
   :children [[re-com/box
               :style {:padding-left "8px"}
               :child [deck-badge deck]]
              [re-com/h-box
               :gap "1em"
               :justify :end
               :align :center
               :children [[:span (count (filter #(contains? (set (:deck-ids % [nil])) (:id deck)) cards))]
                          [re-com/md-icon-button
                           :md-icon-name "zmdi-delete"
                           :style {:float :right}
                           :disabled? (not (:id deck))
                           :on-click #(re-frame/dispatch
                                       [::events/delete-deck (:id deck)])]]]]])

(defn decks-panel
  []
  (let [decks (re-frame/subscribe [::subs/decks])
        cards (re-frame/subscribe [::subs/cards])
        default-deck {:id nil :label "default" :color "ffffff"}]
    [re-com/v-box
     :width "100%"
     :children (into
                [[comps/collapsible-panel
                  :label "New"
                  :gap "1em"
                  :padding "8px"
                  :child [new-deck-panel]]
                 [re-com/scroller
                  :v-scroll :auto
                  :height "450px"
                  :child [re-com/v-box
                          :children (concat
                                     [[deck-item default-deck @cards]]
                                     (for [deck @decks]
                                       ^{:key (:id deck)}
                                       [deck-item deck @cards]))]]])]))

;; main

(defn main-panel []
  (re-frame/dispatch [::events/fetch-cards])
  (re-frame/dispatch [::events/fetch-decks])
  (let [active-tab (re-frame/subscribe [::subs/active-tab])]
    [re-com/v-box
     :height "100%"
     :align :center
     :children [[re-com/v-box
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
                            [re-com/box
                             :style {:border-left "1px solid lightgray"
                                     :border-bottom "1px solid lightgray"
                                     :border-right "1px solid lightgray"}
                             :child (case @active-tab
                                      :cards [cards-panel]
                                      :decks [decks-panel]
                                      :quiz [quiz-panel])]]]]]))
