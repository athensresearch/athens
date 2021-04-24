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


^:cljstyle/ignore
(defn presence-popover-info
  ([] [presence-popover-info @(subscribe [:current-route/uid]) {}])
  ([ctx-uid] [presence-popover-info ctx-uid {}])
  ([ctx-uid {:keys [inline?]}]
   (when (and (subscribe [:db/remote-graph-conf]) (:default? @(subscribe [:db/remote-graph-conf])))
     (let [curr-presence         @(subscribe [:presence/current])
           users-in-cur-uid      (->> curr-presence vals
                                      (filter (fn [u-presence]
                                                (some #(= (% u-presence)
                                                          ctx-uid)
                                                      [:current/uid :editing/uid]))))
           others-in-cur-uid     (filter #(not= (:random/id %)
                                                ws/cur-random)
                                         users-in-cur-uid)
           n-others-in-cur-uid   (count others-in-cur-uid)
           n-users-in-cur-uid    (count users-in-cur-uid)
           show-inline-presence? (pos? n-others-in-cur-uid)]
       (r/with-let [ele (r/atom nil)]
         (when (or (not inline?)
                   (and inline? show-inline-presence?))
           [:<>
            [button
             {:on-mouse-enter #(reset! ele (.-currentTarget %))
              :on-mouse-leave (fn [] (js/setTimeout #(reset! ele nil) 1500))
              :style          (when inline?
                                {:position        "absolute"
                                 :left            "-1.5rem"
                                 :padding-top     "0.5rem"
                                 ::stylefy/manual [[:>svg {:font-size "1rem"}]]})}
             (if inline?
               [:> Group]
               [:<>
                [:> Visibility]
                [:span
                 (cond-> "You"

                   show-inline-presence?
                   (str " and " n-others-in-cur-uid " others"))]])]
            [m-popover
             {:open            (boolean (and show-inline-presence? @ele))
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
                           (= (get-in curr-presence
                                      [ws/cur-random :editing/uid])
                              ctx-uid)))
                  (str "You, ")

                  true (str (->> others-in-cur-uid
                                 (map :name)
                                 (str/join ", ")))

                  (or (not inline?)
                      (and inline? (> n-users-in-cur-uid 1)))
                  (str " are here")

                  (and inline?
                       (= n-users-in-cur-uid 1))
                  (str " is here"))]]]]]))))))

