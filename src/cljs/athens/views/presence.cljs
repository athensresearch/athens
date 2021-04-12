(ns athens.views.presence
  (:require
    ["@material-ui/core/Popover" :as Popover]
    ["@material-ui/icons/Group" :default Group]
    ["@material-ui/icons/GroupWork" :default GroupWork]
    ["@material-ui/icons/Visibility" :default Visibility]
    [athens.style :refer [color]]
    [athens.views.buttons :refer [button]]
    [athens.ws-client :as ws]
    [clojure.string :as str]
    [re-frame.core :refer [subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style]]))


;;-------------------------------------------------------------------
;;--- material ui ---


(def m-popover (r/adapt-react-class (.-default Popover)))


;;-------------------------------------------------------------------
;;--- comps ---


(def info-style
  {:padding         "0.5rem"
   :width           "200px"
   :font-size       "14px"
   :display         "flex"
   :background      (color :background-plus-2)
   :color           (color :body-text-color)
   ::stylefy/manual [[:svg {:margin "auto 10px"}]]})


(defn presence-popover-info
  ([] [presence-popover-info @(subscribe [:current-route/uid]) {}])
  ([ctx-uid] [presence-popover-info ctx-uid {}])
  ([ctx-uid {:keys [inline?]}]
   (when (:default? @(subscribe [:db/remote-graph-conf]))
     (let [curr-presence           @(subscribe [:presence/current])
           others-in-cur-uid       (->> curr-presence vals
                                        (filter #(and ctx-uid
                                                      (or (= (:current/uid %) ctx-uid)
                                                          (= (:editing/uid %) ctx-uid))
                                                      (not= (:random/id %)
                                                            ws/cur-random))))
           others-in-cur-uid-count (count others-in-cur-uid)
           total-in-cur-uid        (->> curr-presence vals
                                        (filter #(and ctx-uid
                                                      (or (= (:current/uid %) ctx-uid)
                                                          (= (:editing/uid %) ctx-uid))))
                                        count)]
       (r/with-let [ele (r/atom nil)]
         (when (or (not inline?)
                   (and inline? (>= others-in-cur-uid-count 1)))
           [:<>
            [button
             {:onClick #(reset! ele (.-currentTarget %))
              :style   (when inline?
                         {:position "absolute"
                          :left "-1.5rem"
                          :padding-top "0.5rem"
                          ::stylefy/manual [[:>svg {:font-size "1rem"}]]})}
             (if inline?
               [:> Group]
               [:<>
                [:> Visibility]
                [:span
                 (cond-> (str "You")
                   (> others-in-cur-uid-count 0) (str " and " others-in-cur-uid-count " others"))]])]
            [m-popover
             {:open            (and (> others-in-cur-uid-count 0) @ele)
              :anchorEl        @ele
              :onClose         #(reset! ele nil)
              :anchorOrigin    #js{:vertical   "bottom"
                                   :horizontal "center"}
              :transformOrigin #js{:vertical   "top"
                                   :horizontal "center"}}
             [:div (use-style info-style)
              [:<>
               [:> GroupWork]
               [:span
                (cond-> ""
                  (or (not inline?)
                      (and inline?
                           (= (get-in curr-presence [ws/cur-random :editing/uid])
                              ctx-uid)))
                  (str "You, ")

                  true (str (->> others-in-cur-uid
                                 (map :name)
                                 (str/join ", ")))

                  (not inline?) (str " are here")

                  (and inline?
                       (> total-in-cur-uid 1)) (str " are here")

                  (and inline?
                       (= total-in-cur-uid 1)) (str " is here"))]]]]]))))))

