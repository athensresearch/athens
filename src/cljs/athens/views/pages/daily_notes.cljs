(ns athens.views.pages.daily-notes
  (:require
   ["@chakra-ui/react" :refer [Box VStack]]
   [athens.dates :as dates]
   [athens.reactive :as reactive]
   [athens.views.pages.header :refer [title-container]]
   [athens.views.pages.node-page :as node-page]
   [re-frame.core :refer [dispatch subscribe]]))


(defn reactive-pull-many
  "Need a reactive pull because block/uid doesn't exist yet in datascript, but is found in :daily-notes/items.
  This happens because (dispatch [:daily-note/next (dates/get-day)]) updates re-frame faster than the datascript tx can happen

  Bug: It's still possible for a day to not get created. The UI for this just shows an empty page without a title. Acceptable bug :)"
  [ids]
  (keep #(reactive/get-reactive-block-document [:block/uid %]) ids))



(defn daily-notes-page
  ([props children]
  (let [{:keys [real?]} props]
    [:> Box {:class "node-page daily-notes"
             :boxShadow "page",
             :bg "background.floor"
             :alignSelf "stretch"
             :justifySelf "stretch"
             :opacity (if real? 1 0.5)
             :px {:sm 6 :md 12}
             :py {:sm 6 :md 12}
             :borderWidth "1px"
             :borderStyle "solid"
             :borderColor "separator.divider"
             :transitionDuration "0s"
             :borderRadius "0.5rem"
             :minHeight "calc(100vh - 10rem)"}
     children])))


;; Components


(defn page
  []
  (let [note-refs (subscribe [:daily-notes/items])]
    (fn []
      (if (empty? @note-refs)
        (dispatch [:daily-note/next (dates/get-day)])
        (let [notes (reactive-pull-many @note-refs)]
          [:> VStack {:id "daily-notes"
                      :minHeight "calc(100vh + 1px)"
                      :display        "flex"
                      :gap            "1.5rem"
                      :py             "2rem"
                      :px             "2rem"
                      :alignItems    "stretch"
                      :flex           "1 1 100%"
                      :flexDirection "column"}
           (doall
            (for [{:keys [block/uid]} notes]
              [daily-notes-page {:key uid
                                 :real? true}
               [node-page/page [:block/uid uid]]]))
           [daily-notes-page {:real? false}
            [title-container "Earlier"]]])))))
