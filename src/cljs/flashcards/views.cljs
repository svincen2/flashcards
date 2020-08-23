(ns flashcards.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [components :as comps]
   [flashcards.subs :as subs]
   [flashcards.views.flashcards :as fc]
   [taoensso.timbre :as log]
   [flashcards.events :as events]
   ))

;; flashcards
(defn flashcards-panel
  []
  [re-com/v-box
   :gap "1em"
   :padding "8px"
   :children [[re-com/hyperlink
               :label "Examples"
               :on-click #(re-frame/dispatch [::events/set-active-panel :home-panel])]
              [fc/flashcards-panel]]])

;; components
(defn components-panel
  []
  [re-com/v-box
   :gap "1em"
   :padding "8px"
   :children [[re-com/title
               :label "Components"
               :level :level1]
              [re-com/hyperlink
               :label "Examples"
               :on-click #(re-frame/dispatch [::events/set-active-panel :home-panel])]
              [comps/collapsible-panel
               :label "collapsible-panel"
               :child [re-com/alert-box
                       :heading "This is just an example"
                       :body "You can put anything you want in this thing!"]]
              [re-com/v-box
               :gap "1em"
               :padding "8px"
               :children [[re-com/label
                           :label "paginated-panels-component"]
                          [re-com/box
                           :style {:border "1px dashed green"
                                   :border-radius "1em"}
                           :child [comps/paginated-panels-component
                                   :children [[re-com/title
                                               :level :level2
                                               :label "Paginate"]
                                              [re-com/title
                                               :level :level2
                                               :label "Between"]
                                              [re-com/title
                                               :level :level2
                                               :label "Panels"]]]]]]]])

;; home
(defn home-panel []
  [re-com/v-box
   :gap "1em"
   :padding "8px"
   :children [[re-com/title
               :label "Examples"
               :level :level1]
              [:ul
               [:li [re-com/hyperlink
                     :label "Flashcards"
                     :on-click #(re-frame/dispatch [::events/set-active-panel :flashcards-panel])]]
               [:li [re-com/hyperlink
                     :label "Components"
                     :on-click #(re-frame/dispatch [::events/set-active-panel :comps-panel])]]]]])

;; main

#_(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :comps-panel [components-panel]
    :flashcards-panel [flashcards-panel]
    [:div "Something went horribly wrong..."]))

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :height "100%"
     :padding "8px"
     :children [(case @active-panel
                  :home-panel [home-panel]
                  :comps-panel [components-panel]
                  :flashcards-panel [flashcards-panel]
                  [:div "Something went horribly wrong..."])
                #_[panels @active-panel]]]))
