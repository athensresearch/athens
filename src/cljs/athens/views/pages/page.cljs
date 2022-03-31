(ns athens.views.pages.page
  (:require
    ["@chakra-ui/react"            :refer [Box]]
    [athens.common-db              :as common-db]
    [athens.db                     :as db]
    [athens.reactive               :as reactive]
    [athens.views.pages.block-page :as block-page]
    [athens.views.pages.node-page  :as node-page]
    [re-frame.core                 :as rf]))


(defn page-container
  [props children]
  (let [{:keys [uid type]} props]
    [:> Box {:as "article"
             :data-ui uid
             :class (str type "-page")
             :flexDirection "column"
             :display "flex"
             :margin "2rem auto"
             :padding "1rem 4rem 10rem"
             :flexBasis "100%"
             :maxWidth "75rem"}
     children]))


(defn page-by-title
  []
  (let [title    (rf/subscribe [:current-route/page-title])
        page-eid (common-db/e-by-av @db/dsdb :node/title @title)]
    (if (int? page-eid)
      [page-container {:uid page-eid :type "node"}
       [node-page/page page-eid]]
      [:h3 (str "404: Page with title '" @title "' doesn't exist")])))


(defn page
  "Can be a block or a node page."
  []
  (let [uid (rf/subscribe [:current-route/uid])
        {:keys [node/title block/string db/id]} (reactive/get-reactive-block-or-page-by-uid @uid)]
    [page-container {:uid @uid :type (if title "node" "block")}
     (cond
       title [node-page/page id]
       string [block-page/page id]
       :else [:h3 "404: This page doesn't exist"])]))
