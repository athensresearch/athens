(ns athens.devcards.block-page
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.db :as db]
    [athens.devcards.blocks :refer [block-el]]
    [athens.router :refer [navigate-page]]
    [athens.style :refer [base-styles]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [datascript.core :as d]
    [devcards.core :refer-macros [defcard defcard-rg]]
    [posh.reagent :refer [transact! posh! pull]]))


(defcard-rg Import-Styles
  [base-styles])


(defcard Instantiate-Dsdb)


(def datoms
  [{:db/id          2381,
    :block/uid      "OaSVyM_nr",
    :block/open     true,
    :node/title     "Athens FAQ",
    :block/children [{:db/id          2158,
                      :block/uid      "BjIm6GeRP",
                      :block/string   "Why open-source?",
                      :block/open     true,
                      :block/order    3,
                      :block/children [{:db/id        2163,
                                        :block/uid    "GNaf3XzpE",
                                        :block/string "The short answer is the security and privacy of your data.",
                                        :block/open   true,
                                        :block/order  1}
                                       {:db/id          2347,
                                        :block/uid      "jbiKpcmIX",
                                        :block/string   "Firstly, I wouldn't be surprised if Roam was eventually open-sourced.",
                                        :block/open     true,
                                        :block/order    0,
                                        :block/children [{:db/id        2176,
                                                          :block/uid    "gVINXaN8Y",
                                                          :block/string "Suffice it to say that Roam being open-source is undeniably something that the team has already considered. Why is it not open-source already? You'd have to ask the Roam team, but Roam, a business, is not obligated to open-source anything.",
                                                          :block/open   true,
                                                          :block/order  2}
                                                         {:db/id          2346,
                                                          :block/uid      "ZOxwo0K_7",
                                                          :block/string   "The conclusion of the [[Roam White Paper]] states that Roam's vision is a collective, \"open-source\" intelligence.",
                                                          :block/open     true,
                                                          :block/order    0,
                                                          :block/children [{:db/id        2174,
                                                                            :block/uid    "WKWPPSYQa",
                                                                            :block/string "((iWmBJaChO))",
                                                                            :block/open   true,
                                                                            :block/order  0}]}
                                                         {:db/id        2349,
                                                          :block/uid    "VQ-ybRmNh",
                                                          :block/string "In the Roam Slack, I recall Conor saying one eventual goal is to work on a protocol that affords interoperability between open source alternatives. I would share the message but can't find it because of Slack's 10k message limit.",
                                                          :block/open   true,
                                                          :block/order  1}
                                                         {:db/id        2351,
                                                          :block/uid    "PGGS8MFH_",
                                                          :block/string "Ultimately, we don't know when/if Roam will be open-sourced, but it's possible that Athens could accelerate or catalyze this. Regardless, there will always be some who are open-source maximalists and some who want to self-host, because that's probably really the most secure thing you can do (if you know what you're doing).",
                                                          :block/open   true,
                                                          :block/order  3}]}]}]}])


(defonce conn (d/create-conn db/schema))
(posh! conn)
(transact! conn datoms)


;; TODO: replace " > " with an icon. Get a TypeError when doing this, though. Maybe same problem as "->" issue in Athena results
(defn block-page-el
  [block parents]
  (let [{:block/keys [string children]} block]
    [:div
     [:span {:style {:color "gray"}}
      (interpose
        " > "
        (for [p parents]
          (let [{:keys [node/title block/uid block/string]} p]
            [:span {:key uid :style {:cursor "pointer"} :on-click #(navigate-page uid)} (or string title)])))]
     [:h1 (str "• " string)]
     [:div (for [child children]
             (let [{:keys [db/id]} child]
               ^{:key id} [block-el child]))]]))


(defn block-page-component
  ""
  [conn ident]
  (let [block   @(pull conn db/block-pull-pattern ident)
        parents (->> @(pull conn db/parents-pull-pattern ident)
                     (db/shape-parent-query))]
    ;;(prn block parents)
    [block-page-el block parents]))


(defcard-rg Block-Page
  "pull entity 2347: a block within Athens FAQ

  two queries:

  1. block+children
  1. parents for context"
  [block-page-component conn 2347])
