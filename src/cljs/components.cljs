(ns components
  (:require [reagent.core :as r]
            [re-com.core :as re-com]))

(defn collapsible-panel
  [& opts]
  (let [{:keys [label child padding]} opts
        collapsed? (r/atom true)]
    (fn [& _]
      [re-com/box
       :padding padding
       :child
       [re-com/border
        :border "1px dashed green"
        :radius "1em"
        :child
        [re-com/v-box
         :padding "8px"
         :children
         [[re-com/h-box
           :gap "0.5em"
           :padding "8px"
           :style {:cursor "pointer"}
           :attr {:on-click #(swap! collapsed? not)}
           :children
           [[re-com/label
             :label label]
            [re-com/md-icon-button
             :md-icon-name (if @collapsed?
                             "zmdi-chevron-down"
                             "zmdi-chevron-up")]]]
          (when-not @collapsed?
            child)]]]])))
