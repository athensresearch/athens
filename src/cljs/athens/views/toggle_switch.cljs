(ns athens.views.toggle-switch
  (:require
    [stylefy.core :as stylefy]))


(defn toggle-switch
  "Styles largely borrowed from https://codesandbox.io/s/athens-settings-page-forked-14432?file=/src/components/ToggleSwitch.jsx
  Thank you Shanberg"
  [props]
  [:input (stylefy/use-style {:appearance      "none"
                              :border-radius   "10em"
                              :height          "1em"
                              :width           "1.615em"
                              :display         "flex"
                              :transition      "all 0.3s ease"
                              :border          "1px solid"
                              :position        "relative"
                              :outline         "none"
                              :box-sizing      "content-box"
                              :background      "var(--background-minus-2)"
                              ::stylefy/manual [[:&:before {:background    "white"
                                                            :content       "''" :position "absolute" :top 0 :bottom 0 :left 0 :right "37%" :box-shadow "0 0 0 1px var(--border-color)"
                                                            :border-radius "inherit" :transition "all 0.15s ease" :z-index 2}]
                                                [:&:checked {:background "var(--link-color)"}
                                                 [:&:before {:left "37%" :right 0}]]]}
                             (merge {:type "checkbox"}
                                    props))])
