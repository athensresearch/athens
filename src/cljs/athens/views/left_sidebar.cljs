(ns athens.views.left-sidebar
  (:require
    ["/components/SidebarShortcuts/Item" :refer [Item]]
    ["/components/SidebarShortcuts/List" :refer [List]]
    ["@chakra-ui/react" :refer [VStack Flex Heading Button Link Flex]]
    ["framer-motion" :refer [AnimatePresence motion]]
    [athens.reactive :as reactive]
    [athens.router   :as router]
    [athens.util     :as util]
    [re-frame.core   :as rf]
    [reagent.core    :as r]))


;; Components

(def expanded-sidebar-width "clamp(12rem, 25vw, 18rem)")


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
         [:> List {:items (clj->js shortcuts)
                   :pl "1.25rem"}
          (doall
           (for [sh shortcuts]
             [:> Item {:item (clj->js sh)
                       :onClick (fn [e]
                                  (let [shift? (.-shiftKey e)]
                                    (rf/dispatch [:reporting/navigation {:source :left-sidebar
                                                                         :target :page
                                                                         :pane   (if shift?
                                                                                   :right-pane
                                                                                   :main-pane)}])
                                    (router/navigate-page (second sh) e)))}]))]]

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
