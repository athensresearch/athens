(ns athens.views.dropdown
  (:require
    [athens.db]
    [athens.style :refer [color DEPTH-SHADOWS ZINDICES]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [garden.selectors :as selectors]
    [stylefy.core :as stylefy]))


;;; Styles


(stylefy/keyframes "dropdown-appear"
                   [:from {:opacity 0
                           :transform "translateY(-10%)"}]
                   [:to {:opacity 1
                         :transform "translateY(0)"}])


(def dropdown-style
  {:display "inline-flex"
   :z-index (:zindex-dropdown ZINDICES)
   :padding "0.25rem"
   :border-radius "calc(0.25rem + 0.25rem)" ;; Button corner radius + container padding makes "concentric" container radius
   :min-height "2em"
   :min-width "2em"
   :animation "dropdown-appear 0.125s"
   :animation-fill-mode "both"
   :background (color :background-plus-2)
   :box-shadow [[(:64 DEPTH-SHADOWS) ", 0 0 0 1px rgba(0, 0, 0, 0.05)"]]
   :flex-direction "column"})


(def menu-style
  {:display "grid"
   :grid-gap "0.125rem"
   :min-width "9em"
   :align-items "stretch"
   :grid-auto-flow "row"
   :overflow "auto"
   ::stylefy/manual [[(selectors/& (selectors/not (selectors/first-child))) {:margin-block-start "0.25rem"}]
                     [(selectors/& (selectors/not (selectors/last-child))) {:margin-block-end "0.25rem"}]
                     [:button {:min-height "1.5rem"}
                      [:svg:first-child {:font-size "16px"
                                         :margin-inline-start "0"
                                         :margin-inline-end "0.5rem"}]]]})


#_(def menu-heading-style
    {:min-height "2rem"
     :text-align "center"
     :padding "0.375rem 0.5rem"
     :display "flex"
     :align-content "flex-end"
     :justify-content "center"
     :align-items "center"
     :font-size "12px"
     :max-width "100%"
     :overflow "hidden"
     :text-overflow "ellipsis"})


(def menu-separator-style
  {:border "0"
   :background (color :border-color)
   :align-self "stretch"
   :justify-self "stretch"
   :height "1px"
   :margin "0.25rem 0"})


#_(def submenu-indicator-style
    {:margin-left "auto"
     :opacity "0.5"
     :display "flex"
     :order 10
     :align-self "flex-end"
     :font-family "inherit"
     ::stylefy/manual [[:&:last-child {:padding-inline-end "0"}]]})


;;; Components
;;
;;
;;(defn block-context-menu-component
;;  [style]
;;  [dropdown {:style style :content
;;             [menu {:content
;;                    [:<>
;;                     ;;  [menu-heading "Modify Block 'Day of Datomic On-Prem 2016'"]
;;                     ;;  [textinput {:icon [:> mui-icons/Face] :placeholder "Type to filter"}]
;;                     [button [:<> [:> mui-icons/Link] [:span "Copy Page Reference"]]]
;;                     [button [:<> [:> mui-icons/Star] [:span "Add to Shortcuts"]]]
;;                     [button [:<> [:> mui-icons/Face] [:span "Add Reaction"] [submenu-indicator]]]
;;                     [menu-separator]
;;                     [button [:<> [:> mui-icons/LastPage] [:span "Open in Sidebar"] [:kbd "shift-click"]]]
;;                     [button [:<> [:> mui-icons/Launch] [:span "Open in New Window"] [:kbd "ctrl-o"]]]
;;                     [button [:<> [:> mui-icons/UnfoldMore] [:span "Expand All"]]]
;;                     [button [:<> [:> mui-icons/UnfoldLess] [:span "Collapse All"]]]
;;                     [button [:<> [:> mui-icons/Slideshow] [:span "View As"] [submenu-indicator]]]
;;                     [menu-separator]
;;                     [button [:<> [:> mui-icons/FileCopy] [:span "Duplicate and Break Links"]]]
;;                     [button [:<> [:> mui-icons/LibraryAdd] [:span "Save as Template"]]]
;;                     [button [:<> [:> mui-icons/History] [:span "Browse Versions"]]]
;;                     [button [:<> [:> mui-icons/CloudDownload] [:span "Export As"]]]]}]}])
;;
;;
;;(def items
;;  {"Amet"   {:count 6 :state :added}
;;   "At"     {:count 130 :state :excluded}
;;   "Diam"   {:count 6}
;;   "Donec"  {:count 6}
;;   "Elit"   {:count 30}
;;   "Elitudomin mesucen defibocutruon"  {:count 1}
;;   "Erat"   {:count 11}
;;   "Est"    {:count 2}
;;   "Eu"     {:count 2}
;;   "Ipsum"  {:count 2 :state :excluded}
;;   "Magnis" {:count 10 :state :added}
;;   "Metus"  {:count 29}
;;   "Mi"     {:count 7 :state :added}
;;   "Quam"   {:count 1}
;;   "Turpis" {:count 97}
;;   "Vitae"  {:count 1}})
;;
;;
;;(defn filter-dropdown-component
;;  []
;;  [dropdown {:style   {:width "20em" :height "20em"}
;;             :content [:<>
;;                       [menu-heading "Filters"]
;;                       [filters-el "((some-uid))" items]]}])
