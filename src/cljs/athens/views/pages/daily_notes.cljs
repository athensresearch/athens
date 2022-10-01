(ns athens.views.pages.daily-notes
  (:require
    ["/components/Page/Page" :refer [Page PageHeader PageOverline TitleContainer]]
    ["@chakra-ui/react" :refer [Box VStack]]
    ["react" :as react]
    ["react-intersection-observer" :refer [useInView]]
    [athens.dates :as dates]
    [athens.reactive :as reactive]
    [athens.views.pages.node-page :as node-page]
    [re-frame.core :refer [dispatch subscribe]]))


(defn reactive-pull-many
  "Need a reactive pull because block/uid doesn't exist yet in datascript, but is found in :daily-notes/items.
  This happens because (dispatch [:daily-note/next (dates/get-day)]) updates re-frame faster than the datascript tx can happen

  Bug: It's still possible for a day to not get created. The UI for this just shows an empty page without a title. Acceptable bug :)"
  [ids]
  (->> ids
       (keep #(reactive/get-reactive-block-document [:block/uid %]))
       (filter :block/uid)))


;; Components


(defn page
  []
  (let [note-refs (subscribe [:daily-notes/items])
        get-another-note #(dispatch [:daily-note/next (dates/get-day (dates/uid-to-date %) 1)])]
    (fn []
      (when (empty? @note-refs)
        (dispatch [:daily-note/next (dates/get-day)]))
      (let [notes (reactive-pull-many @note-refs)
            [ref in-view?] (useInView {:delay 250})
            _ (react/useLayoutEffect
                (fn []
                  (when (and (last @note-refs) in-view?) (get-another-note (last @note-refs)))
                  js/undefined)
                #js [in-view? note-refs])]
        [:> VStack {:align "stretch"
                    :alignSelf "stretch"
                    :spacing 4
                    :pt "4rem"
                    :px 4}
         (doall
           (for [{:keys [block/uid]} notes]
             ^{:key uid}
             [node-page/page
              [:block/uid uid]
              {:variant "elevated"
               :alignSelf "stretch"
               :minHeight "calc(var(--app-height) - 6rem)"}]))

         [:> Page {:minHeight "calc(var(--app-height) - 6rem)"
                   :alignSelf "stretch"
                   :variant "elevated"}
          [:> Box {:ref ref}]
          [:> PageHeader
           [:> PageOverline "Daily Notes"]
           [:> TitleContainer "Loading..."]]]]))))
