(ns athens.views.left-sidebar
  (:require
    [athens.db :as db]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color OPACITIES]]
    [athens.util :refer [mouse-offset vertical-center]]
    ;;[athens.views.buttons :refer [button]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [posh.reagent :refer [q]]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]
    [stylefy.core :as stylefy :refer [use-style use-sub-style]]))


;;; Styles


(def left-sidebar-style
  {:width 0
   :grid-area "left-sidebar"
   :height "100%"
   :display "flex"
   :flex-direction "column"
   :overflow-x "hidden"
   :overflow-y "auto"
   :transition "width 0.5s ease"
   ::stylefy/sub-styles {:top-line {:margin-bottom "2.5rem"
                                    :display "flex"
                                    :flex "0 0 auto"
                                    :justify-content "space-between"}
                         :footer {:flex "0 0 auto"
                                  :margin "auto 2rem 0"
                                  :align-self "stretch"
                                  :display "grid"
                                  :grid-auto-flow "column"
                                  :grid-template-columns "1fr auto auto"
                                  :grid-gap "0.25rem"}
                         :small-icon {:font-size "16px"}
                         :large-icon {:font-size "22px"}}
   ::stylefy/manual [[:&.is-open {:width "18rem"}]
                     [:&.is-closed {:width "0"}]]})


(def left-sidebar-content-style
  {:width "18rem"
   :height "100%"
   :display "flex"
   :flex-direction "column"
   :padding "7.5rem 0 1rem"
   :transition "opacity 0.5s ease"
   :opacity 0
   ::stylefy/manual [[:&.is-open {:opacity 1}]
                     [:&.is-closed {:opacity 0}]]})


(def shortcuts-list-style
  {:flex "1 1 100%"
   :display "flex"
   :list-style "none"
   :flex-direction "column"
   :padding "0 2rem"
   :margin "0 0 2rem"
   :overflow-y "auto"
   ::stylefy/sub-styles {:heading {:flex "0 0 auto"
                                   :opacity (:opacity-med OPACITIES)
                                   :line-height "1"
                                   :margin "0 0 0.25rem"
                                   :font-size "inherit"}}})


(def shortcut-style
  {:color (color :link-color)
   :cursor "pointer"
   :display "flex"
   :flex "0 0 auto"
   :padding "0.25rem 0"
   :transition "all 0.05s ease"
   ::stylefy/mode [[:hover {:opacity (:opacity-high OPACITIES)}]]})


(def notional-logotype-style
  {:font-family "IBM Plex Serif"
   :font-size "18px"
   :opacity (:opacity-med OPACITIES)
   :letter-spacing "-0.05em"
   :font-weight "bold"
   :text-decoration "none"
   :justify-self "flex-start"
   :color (color :header-text-color)
   :transition "all 0.05s ease"
   ::stylefy/mode [[:hover {:opacity (:opacity-high OPACITIES)}]]})


(def version-style
  {:color "inherit"
   :text-decoration "none"
   :opacity 0.3
   ::stylefy/mode [[:hover {:opacity (:opacity-high OPACITIES)}]]})


;;; Components


(defn shortcut-component
  [[_ _ _]]
  (let [drag (r/atom nil)]
    (fn [[order title uid]]
      [:li
       [:a (use-style (merge shortcut-style
                             (case @drag
                               :above {:border-top [["1px" "solid" (color :link-color)]]}
                               :below {:border-bottom [["1px" "solid" (color :link-color)]]}
                               {}))
                      {:on-click      (fn [e] (navigate-uid uid e))
                       :draggable     true
                       :on-drag-over  (fn [e]
                                        (.. e preventDefault)
                                        (let [offset       (mouse-offset e)
                                              middle-y     (vertical-center (.. e -target))
                                     ;; find closest li because sometimes event.target is anchor tag
                                     ;; if nextSibling is null, then target is last li and therefore end of list
                                              closest-li   (.. e -target (closest "li"))
                                              next-sibling (.. closest-li -nextElementSibling)
                                              last-child?  (nil? next-sibling)]
                                          (cond
                                            (> middle-y (:y offset)) (reset! drag :above)
                                            (and (< middle-y (:y offset)) last-child?) (reset! drag :below))))
                       :on-drag-start (fn [e]
                                        (set! (.. e -dataTransfer -dropEffect) "move")
                                        (.. e -dataTransfer (setData "text/plain" order)))
                       :on-drag-end   (fn [_])
                       :on-drag-leave (fn [_] (reset! drag nil))
                       :on-drop       (fn [e]
                                        (let [source-order (js/parseInt (.. e -dataTransfer (getData "text/plain")))]
                                          (prn source-order order)
                                          (cond
                                            (= source-order order) nil
                                            (and (= source-order (dec order)) (= @drag :above)) nil
                                            (= @drag :below) (dispatch [:left-sidebar/drop-below source-order order])
                                            :else (dispatch [:left-sidebar/drop-above source-order order])))
                                        (reset! drag nil))})
        title]])))


(defn left-sidebar
  []
  (let [open? (subscribe [:left-sidebar/open])
        shortcuts (->> @(q '[:find ?order ?title ?uid
                             :where
                             [?e :page/sidebar ?order]
                             [?e :node/title ?title]
                             [?e :block/uid ?uid]] db/dsdb)
                       seq
                       (sort-by first))]
    ;; (when @open?

      ;; IF EXPANDED
    [:div (use-style left-sidebar-style {:class (if @open? "is-open" "is-closed")})
     [:div (use-style left-sidebar-content-style {:class (if @open? "is-open" "is-closed")})

       ;; SHORTCUTS
      [:ol (use-style shortcuts-list-style)
       [:h2 (use-sub-style shortcuts-list-style :heading) "Shortcuts"]
       (doall
         (for [sh shortcuts]
           ^{:key (str "left-sidebar-" (second sh))}
           [shortcut-component sh]))]

       ;; LOGO + BOTTOM BUTTONS
      [:footer (use-sub-style left-sidebar-style :footer)
       [:a (use-style notional-logotype-style {:href "https://github.com/athensresearch/athens" :target "_blank"}) "Athens"]
       [:h5 (use-style {:align-self "center"})
        [:a (use-style version-style {:href "https://github.com/athensresearch/athens/blob/master/CHANGELOG.md"
                                      :target "_blank"})
         (athens.util/athens-version)]]]]]))

