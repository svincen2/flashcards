(ns components
  (:require [reagent.core :as r]
            [re-com.core :as re-com]
            [taoensso.timbre :as log]))

(defn collapsible-panel
  [& _]
  (let [collapsed? (r/atom true)]
    (fn [& opts]
      (let [{:keys [label child padding on-click disabled?]} opts
            cursor (if disabled? "not-allowed" "pointer")]
        [re-com/box
         :padding padding
         :child [re-com/v-box
                 :padding "8px"
                 :style {:border "1px dashed green"
                         :border-radius "0.5em"}
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
                              child)]]]))))

(defn paginated-panels-component
  [& opts]
  (let [{:keys [children]} opts
        max-child (dec (count children))
        next-child (r/atom 0)]
    (fn [& opts]
      (let [{:keys [children on-click]} opts]
        [re-com/v-box
         :gap "1em"
         :align :center
         :children [(get children @next-child)
                    [re-com/h-box
                     :gap "1em"
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
