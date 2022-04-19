(ns athens.views.pages.node-preview
  (:require ["/components/Page/Page" :refer [PageFooter]]
            ["@chakra-ui/react" :refer [Box Heading]]
            [athens.common.sentry :refer-macros [wrap-span-no-new-tx]]
            [athens.parse-renderer :as parse-renderer]
            [athens.reactive :as reactive]
            [athens.views.blocks.core :as blocks]
            [athens.views.hoc.perf-mon :as perf-mon]
            [reagent.core :as r]))


;; Helpers

(declare init-state)

(defn sync-title
  "Ensures :title/initial is synced to node/title.
  Cases:
  - User opens a page for the first time.
  - User navigates from a page to another page.
  - User merges current page with existing page, navigating to existing page."
  [title state]
  (when (not= title (:title/initial @state))
    (swap! state assoc :title/initial title :title/local title)))

(defn node-page-el
  [_]
  (let [state         (r/atom init-state)
        unlinked-refs (r/atom [])
        block-uid     (r/atom nil)]
    (fn [node]
      (when (not= @block-uid (:block/uid node))
        (reset! state init-state)
        (reset! unlinked-refs [])
        (reset! block-uid (:block/uid node)))
      (let [{:block/keys [children uid] title :node/title} node]

        (sync-title title state)

        [:<>

         ;; Header
        [:> Heading {:as "h2" :size "md" :mb 2}
         [parse-renderer/parse-and-render (:title/local @state) uid]]

        [:> Box {:maxHeight "10rem" :overflow "hidden"}
          ;; Children
         (for [{:block/keys [uid] :as child} (take 5 children)]
           ^{:key uid}
           [perf-mon/hoc-perfmon {:span-name "block-el"}
            [blocks/block-el child]])]

        [:> PageFooter
         [:> Box {:borderTop "1px solid"
                  :color "foreground.secondary"
                  :mt 2
                  :pt 2
                  :borderColor "separator.divider"}
          (count (wrap-span-no-new-tx "get-reactive-linked-references"
                                      (reactive/get-reactive-linked-references [:node/title title])))
          " Linked references"]]]))))


(defn page
  [ident]
  (let [node (wrap-span-no-new-tx "db/get-reactive-node-document"
                                  (reactive/get-reactive-node-document ident))]
    [node-page-el node]))
