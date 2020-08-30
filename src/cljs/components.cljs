(ns components
  (:require [reagent.core :as r]
            [re-com.core :as re-com]
            [taoensso.timbre :as log]
            [clojure.string :as str]
            [utils :as utils]))

(defn collapsible-panel
  [& _]
  (let [collapsed? (r/atom true)]
    (fn [& opts]
      (let [{:keys [label child padding on-click disabled?]} opts
            cursor (if disabled? "not-allowed" "pointer")]
        [re-com/v-box
         :padding padding
         :style {:border-bottom "1px solid lightgray"}
         :children [[re-com/h-box
                     :gap "0.5em"
                     :padding "8px"
                     :style {:cursor cursor}
                     :attr {:on-click #(do (when on-click (on-click))
                                           (swap! collapsed? not))}
                     :children [[re-com/label
                                 :label label
                                 :style {:color (if disabled? "lightgray" "gray")}]
                                [re-com/md-icon-button
                                 :md-icon-name (if (or @collapsed? disabled?)
                                                 "zmdi-chevron-down"
                                                 "zmdi-chevron-up")
                                 :style {:cursor cursor}
                                 :disabled? disabled?]]]
                    (when-not (or @collapsed? disabled?)
                      child)]]))))

(defn paginated-panels-component
  [& opts]
  (let [{:keys [children]} opts
        max-child (dec (count children))
        next-child (r/atom 0)]
    (fn [& opts]
      (let [{:keys [children on-click]} opts]
        [re-com/v-box
         :align :center
         :children [(get children @next-child)
                    [re-com/line]
                    [re-com/h-box
                     :gap "1em"
                     :padding "8px"
                     :children [[re-com/md-icon-button
                                 :md-icon-name "zmdi-chevron-left"
                                 :disabled? (= @next-child 0)
                                 :on-click #(do
                                             (when (> @next-child 0)
                                               (swap! next-child dec))
                                             (when on-click (on-click)))]
                                [re-com/md-icon-button
                                 :md-icon-name "zmdi-chevron-right"
                                 :disabled? (= @next-child max-child)
                                 :on-click #(do
                                             (when (< @next-child max-child)
                                               (swap! next-child inc))
                                             (when on-click (on-click)))]]]]]))))

(defn color-picker-slider
  [& opts]
  (let [{:keys [label model on-change]} opts]
    [re-com/h-box
     :gap "1em"
     :children [[re-com/slider
                 :model model
                 :width "250px"
                 :on-change #(do
                               (reset! model %)
                               (when on-change
                                 (on-change)))
                 :min 0
                 :max 255
                 :step 1]
                [re-com/label :label label]]]))

(defn color-picker
  [model]
  (let [[r g b] (utils/hex-color->rgb-bytes @model)
        red (r/atom r)
        green (r/atom g)
        blue (r/atom b)
        on-change #(reset! model (utils/rgb->hex @red @green @blue))]
    (fn [_]
      [re-com/v-box
       :gap "1em"
       :align :end
       :children [[re-com/box
                   :height "32px"
                   :width "64px"
                   :child [:div {:style {:background-color (str "#" (utils/rgb->hex @red @green @blue))
                                         :display :block
                                         :width "100%"
                                         :height "100%"
                                         :border "1px solid lightgray"
                                         :border-radius "0.5em"}}]]
                  [re-com/v-box
                   :gap "1em"
                   :children [[color-picker-slider
                               :label "r"
                               :model red
                               :on-change on-change]
                              [color-picker-slider
                               :label "g"
                               :model green
                               :on-change on-change]
                              [color-picker-slider
                               :label "b"
                               :model blue
                               :on-change on-change]]]]])))
