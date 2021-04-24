(ns athens.views.pages.page
  (:require
    [athens.db :as db]
    [athens.views.pages.block-page :as block-page]
    [athens.views.pages.node-page :as node-page]
    [posh.reagent :refer [pull]]
    [re-frame.core :refer [subscribe dispatch] :as rf]))


(defn page
  []
  (let [uid (subscribe [:current-route/uid])
        {:keys [node/title block/string db/id]} @(pull db/dsdb '[*] [:block/uid @uid])]
    (cond
      title [node-page/page id]
      string [block-page/page id]
      :else [:h3 "404: This page doesn't exist"])))
