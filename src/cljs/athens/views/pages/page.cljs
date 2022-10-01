(ns athens.views.pages.page
  (:require
    ["/components/Page/Page"      :refer [PageNotFound]]
    [athens.common-db             :as    common-db]
    [athens.db                    :as    db]
    [athens.reactive              :as    reactive]
    [athens.router                :as    router]
    [athens.views.blocks.core     :as    blocks]
    [athens.views.pages.node-page :as    node-page]
    [re-frame.core                :as    rf]))


(defn page-by-title
  []
  (let [title    (rf/subscribe [:current-route/page-title])
        page-eid (common-db/e-by-av @db/dsdb :node/title @title)]
    (if (int? page-eid)
      [node-page/page page-eid {:pt "1.75rem"}]
      [:> PageNotFound {:title @title
                        :onClickHome #(router/navigate :pages)}])))


(defn page
  "Can be a block or a node page."
  []
  (let [uid (rf/subscribe [:current-route/uid])
        {:keys [node/title block/string db/id]} (reactive/get-reactive-block-or-page-by-uid @uid)]
    (cond
      title [node-page/page id {:pt "1.75rem"}]
      string [blocks/page id]
      :else [:> PageNotFound {:onClickHome #(router/navigate :pages)}])))
