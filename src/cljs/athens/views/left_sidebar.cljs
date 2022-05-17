(ns athens.views.left-sidebar
  (:require
    ["@chakra-ui/react" :refer [VStack Flex Heading Button Link Flex]]
    ["framer-motion" :refer [AnimatePresence motion]]
    [athens.reactive :as reactive]
    [athens.router   :as router]
    [athens.util     :as util]
    [re-frame.core   :as rf]
    [reagent.core    :as r]))


;; Components

(def expanded-sidebar-width "clamp(12rem, 25vw, 18rem)")


(defn shortcut-component
  [_]
  (let [drag (r/atom nil)]
    (fn [[order title]]
      [:> Flex {:as "li"
                :align "stretch"
                :border "1px solid transparent"
                :borderTopColor (when (:above @drag) "brand")
                :borderBottomColor (when (:below @drag) "brand")}
       [:> Button {:variant "ghost"
                   :mx "1.25rem"
                   :size "sm"
                   :display "inline-block"
                   :textAlign "left"
                   :justifyContent "flex-start"
                   :overflow "hidden"
                   :fontWeight "normal"
                   :whiteSpace "nowrap"
                   :textOverflow "ellipsis"
                   :flex "1"
                   :boxShadow "0 0 0 0.25rem transparent"
                   :_focus {:outline "none"}
                   :_active {:transitionDuration "0s"}
                   :on-click (fn [e]
                               (let [shift? (.-shiftKey e)]
                                 (rf/dispatch [:reporting/navigation {:source :left-sidebar
                                                                      :target :page
                                                                      :pane   (if shift?
                                                                                :right-pane
                                                                                :main-pane)}])
                                 (router/navigate-page title e)))
                   :draggable     true
                   :on-drag-over  (fn [e]
                                    (.. e preventDefault)
                                    (let [offset       (util/mouse-offset e)
                                          middle-y     (util/vertical-center (.. e -target))
                                          ;; find closest li because sometimes event.target is anchor tag
                                          ;; if nextSibling is null, then target is last li and therefore end of list
                                          closest-li   (.. e -target (closest "li"))
                                          next-sibling (.. closest-li -nextElementSibling)
                                          last-child?  (nil? next-sibling)]
                                      (cond
                                        (> middle-y (:y offset))                   (reset! drag :above)
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
                                        (and (= source-order
                                                (dec order))
                                             (= @drag :above)) nil
                                        (= @drag :below)       (rf/dispatch [:left-sidebar/drop source-order order :after])
                                        :else                  (rf/dispatch [:left-sidebar/drop source-order order :before])))
                                    (reset! drag nil))}
        title]])))


(defn left-sidebar
  []
  (let [open?     (rf/subscribe [:left-sidebar/open])
        shortcuts (reactive/get-reactive-shortcuts)]
    [:> AnimatePresence {:initial false}
     (when @open?
       [:> (.-div motion)
        {:style {:display "flex"
                 :flex-direction "column"
                 :height "100%"
                 :zIndex 1
                 :alignItems "stretch"
                 :gridArea "left-sidebar"
                 :position "relative"
                 :overflow "hidden"}
         :initial {:width 0
                   :opacity 0}
         :animate {:width expanded-sidebar-width
                   :opacity 1}
         :exit {:width 0
                :opacity 0}}

        ;; SHORTCUTS
        [:> VStack {:as "ol"
                    :align "stretch"
                    :width expanded-sidebar-width
                    :py "1rem"
                    :marginTop "7rem"
                    :spacing "0.25rem"
                    :overflowY "auto"
                    :backdropFilter "blur(1em)"
                    :borderRadius "lg"
                    :sx {"@supports (overflow-y: overlay)" {:overflowY "overlay"}
                         :listStyle "none"
                         :WebkitAppRegion "no-drag"}}
         [:> Heading {:as "h2"
                      :px "2rem"
                      :pb "0.5rem"
                      :size "sm"
                      :color "foreground.secondary"}
          "Shortcuts"]
         (doall
           (for [sh shortcuts]
             ^{:key (str "left-sidebar-" (second sh))}
             [shortcut-component sh]))]

        ;; LOGO + BOTTOM BUTTONS
        [:> Flex {:as "footer"
                  :width expanded-sidebar-width
                  :flexWrap "wrap"
                  :gap "0.25em 0.5em"
                  :fontSize "sm"
                  :p "2rem"
                  :mt "auto"}
         [:> Link {:fontWeight "bold"
                   :display "inline-block"
                   :href "https://github.com/athensresearch/athens/issues/new/choose"
                   :target "_blank"}
          "Athens"]
         [:> Link {:color "foreground.secondary"
                   :display "inline-block"
                   :href "https://github.com/athensresearch/athens/blob/master/CHANGELOG.md"
                   :target "_blank"}
          (athens.util/athens-version)]]])]))
