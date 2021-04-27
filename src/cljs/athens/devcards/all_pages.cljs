(ns athens.devcards.all-pages
  (:require
    [athens.db :as db]
    [athens.devcards.db :refer [load-real-db-button]]
    [athens.views.buttons :refer [button]]
    [athens.views.pages.all-pages :as all-pages]
    [datascript.core :as d]
    [devcards.core :refer [defcard defcard-rg]]
    [garden.core :refer [css]]
    [tick.locale-en-us]))


(defcard "# All Pages — [#100](https://github.com/athensresearch/athens/issues/100)")


(defcard-rg Import-Styles
  [:style (css [:.com-rigsomelight-devcards-container {:width "90%"}])])


(defcard-rg Create-Page
  "Page title increments by more than one each time because we create multiple entities (the child blocks)."
  [button {:on-click (fn []
                       (let [n (:max-eid @db/dsdb)]
                         (d/transact! db/dsdb [{:node/title     (str "Test Title " n)
                                                :block/uid      (str "uid" n)
                                                :block/children [{:block/string "a block string" :block/uid (str "uid-" n "-" (rand))}]
                                                :create/time    (.getTime (js/Date.))
                                                :edit/time      (.getTime (js/Date.))}])))}
   "Create Page"])


(defcard-rg Load-Real-DB
  [load-real-db-button])


(defcard-rg Table
  [all-pages/page])
