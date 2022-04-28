(ns athens.bot
  (:require [cljs-http.client :as http]
            [re-frame.core :as rf]
            [athens.common-db :as common-db]
            [athens.db :as db]
            [athens.common-events.graph.atomic :as atomic-graph-ops]
            [athens.common-events.graph.ops :as graph-ops]
            [athens.common.utils :as common.utils]
            [cljs.core.async :refer [<!]]
            [athens.common-events.graph.composite :as composite]
            [athens.common-events :as common-events])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def users
  {"[[@Jeff]]"        "<@!374269202103664651>"
   "[[@Alex]]"        "<@!776180002722021396>"
   "[[@Sid]]"         "<@!701431351387226143>"
   "[[@Filipe]]"      "<@!469099065712312320>"
   "[[@Stuart]]"      "<@!229346773104066562>"
   "[[@Athens Team]]" "<@&858004385215938560>"})


(def athens-users
  ["@Stuart" "@Alex" "@Jeff" "@Filipe" "@Sid"])


(defn parse-for-mentions
  [content]
  (loop [parsed content
         users users]
    (if (empty? users)
      parsed
      (recur (clojure.string/replace parsed (ffirst users) (second (first users)))
             (drop 1 users)))))


(defn create-block-new-block-save-op
  [db new-block-uid position save-string]
  (let [new-block-op       (atomic-graph-ops/make-block-new-op new-block-uid position)
        new-block-save-op  (graph-ops/build-block-save-op @db/dsdb new-block-uid save-string)]
    [new-block-op
     new-block-save-op]))


(defn create-notification-event
  [db parent sender receiver ref]
  (let [receiver-page-uid                         (common-db/get-page-uid @db/dsdb receiver)
        {old-to-read-block-uid :block/uid
         to-read-block-string  :block/string }    (common-db/nth-child @db/dsdb receiver-page-uid 0)
        to-read-block-uid                         (if (not= "To read" to-read-block-string)
                                                    (common.utils/gen-block-uid)
                                                    old-to-read-block-uid)
        ref-parent-string                        (if (:node/title parent)
                                                   (str "**[[" (:node/title parent) "]]**" "\n")
                                                   (str "**((" (:block/uid parent) "))**" "\n"))

        new-notif-op                             (concat (if (not= "To read" to-read-block-string)
                                                           ;; If there is no "To read" block then create it first
                                                           (create-block-new-block-save-op db
                                                                                           to-read-block-uid
                                                                                           (common-db/compat-position @db/dsdb {:block/uid receiver-page-uid
                                                                                                                                :relation  :first})
                                                                                           "To read")
                                                           [])
                                                         (create-block-new-block-save-op db
                                                                                         (common.utils/gen-block-uid)
                                                                                         (common-db/compat-position @db/dsdb {:block/uid to-read-block-uid
                                                                                                                              :relation  :first})
                                                                                         (str ref-parent-string
                                                                                              "*[[" sender "]]: ((" ref ")) *")))]
    new-notif-op))


(defn create-notifs-ops
  [db parent sender receivers ref]
  (mapcat
    (fn [receiver]
      (let [receiver-page? (common-db/get-page @db/dsdb [:node/title receiver])]
        (when receiver-page?
          (create-notification-event db parent sender receiver ref))))
    receivers))


(rf/reg-event-fx
  :check-for-mentions
  (fn [{:keys [db]} [_ uid block-string author]]
    (let [block-parent         (common-db/get-parent @db/dsdb [:block/uid uid])
          is-parent-a-new-task (contains? #{"To Do 🤔" "Todo"} block-parent)
          mentions             (remove nil? (into #{} (map
                                                        #(when (clojure.string/includes? block-string %)
                                                           %)
                                                        athens-users)))]
      (cond
        (true? is-parent-a-new-task) {:fx [[:dispatch [:prepare-message uid author :new-task {:string block-string}]]]}
        (seq mentions)    {:fx [[:dispatch [:notification/send-to-athens block-parent author mentions uid]]
                                [:dispatch [:prepare-message uid author :mention {:string block-string}]]]}))))


(rf/reg-event-fx
  :move-ticket
  (fn [{:keys [db]} [_ source-uid target-uid author]]
    (let [source-string         (:block/string (common-db/get-block @db/dsdb [:block/uid source-uid]))
          source-parent-string  (:block/string (common-db/get-parent @db/dsdb [:block/uid source-uid]))
          target-parent-string  (:block/string (common-db/get-parent @db/dsdb [:block/uid target-uid]))
          todo                  #{"To Do 🤔" "Todo"}
          doing                 #{"Doing \uD83E\uDD13" "Doing"}
          done                  #{"Done \uD83C\uDF89" "Done"}
          blocked               #{"Blocked ✋" "Blocked"}
          code-review           #{"Code Review \uD83D\uDE2C" "Code review"}
          blocked-by-discussion #{"Blocked by Discussion ✋" "Blocked by Discussion"}
          eng-testing           #{"Eng Testing \uD83E\uDD1E"}
          action-data           (cond
                                  (and (contains? todo source-parent-string)
                                       (contains? doing target-parent-string))                 {:msg    "** Todo to Doing**"
                                                                                                :string source-string}
                                  (and (contains? todo source-parent-string)
                                       (contains? done target-parent-string))                  {:msg    "** Todo to Done**"
                                                                                                :string source-string}
                                  (and (contains? doing source-parent-string)
                                       (contains? done target-parent-string))                  {:msg    "** Doing to Done**"
                                                                                                :string source-string}
                                  (and (contains? doing source-parent-string)
                                       (contains? blocked target-parent-string))               {:msg    "** Doing to Blocked**"
                                                                                                :string source-string}
                                  (and (contains? blocked source-parent-string)
                                       (contains? done target-parent-string))                  {:msg    "** Blocked to Done**"
                                                                                                :string source-string}
                                  (and (contains? doing source-parent-string)
                                       (contains? code-review target-parent-string))           {:msg    "** Doing to Code review**"
                                                                                                :string source-string}
                                  (and (contains? code-review source-parent-string)
                                       (contains? done target-parent-string))                  {:msg    "** Code review to Done**"
                                                                                                :string source-string}
                                  (and (contains? todo source-parent-string)
                                       (contains? blocked-by-discussion target-parent-string)) {:msg    "** Todo to Blocked by discussion**"
                                                                                                :string source-string}
                                  (and (contains? doing source-parent-string)
                                       (contains? blocked-by-discussion target-parent-string)) {:msg    "** Doing to Blocked by discussion**"
                                                                                                :string source-string}
                                  (and (contains? blocked-by-discussion source-parent-string)
                                       (contains? done target-parent-string))                  {:msg    "** Blocked by discussion to Done**"
                                                                                                :string source-string}
                                  (and (contains? doing source-parent-string)
                                       (contains? eng-testing target-parent-string))           {:msg    "** Doing to Eng testing**"
                                                                                                :string source-string}
                                  (and (contains? eng-testing source-parent-string)
                                       (contains? done target-parent-string))                  {:msg    "** Eng testing to Done**"
                                                                                                :string source-string}
                                  :else                                                        nil)]
      (when action-data
        {:fx [[:dispatch [:prepare-message source-uid author :board action-data]]]}))))

(rf/reg-event-fx
  :prepare-message
  (fn [_ [_ uid author action action-data]]
    "action-data for:
    Comments: comment string)
    Mention:  mention who, mention message)
    Board:    move state, block content"

    (let [parsed-string        (parse-for-mentions (:string action-data))
          full-url             (.. js/window -location -href)
          base-url             (first (clojure.string/split full-url "#"))
          block-parent-url     (str base-url "#/page/" uid)
          message              {"message"
                                (cond
                                  (= action
                                     :comment)     (str author " wrote a comment: " "\""
                                                        parsed-string
                                                        "\"" " — " block-parent-url)
                                  (= action
                                     :mention)     (str "**" author "** "
                                                        parsed-string
                                                        "--" block-parent-url)

                                  (= action
                                     :new-task)     (str "**" author " spawned a new task** "
                                                         parsed-string
                                                         "--" block-parent-url)
                                  (and (= action
                                        :board))   (str "**"author"**" " Moved -- "
                                                        "*" parsed-string "* "
                                                        " -- from "  (:msg action-data)
                                                        " — " block-parent-url))}]
      {:fx [[:dispatch [:notification/send-to-discord message]]]})))


(rf/reg-event-fx
  :notification/send-to-athens
  (fn [{:keys [db]} [_ parent sender receivers ref]]
    (let [notif-ops        (create-notifs-ops db parent sender receivers ref)
          notification-op  (composite/make-consequence-op {:op/type :notification}
                                                          notif-ops)
          event            (common-events/build-atomic-event notification-op)]
      {:fx [[:dispatch [:resolve-transact-forward event]]]})))


(rf/reg-event-fx
  :notification/send-to-discord
  (fn [_ [_ message]]
    (println "send notification" message)
    {:discord-bot message}))


(rf/reg-event-fx
  :notification/take-to-user-page
  (fn [_ _]
    (let [current-user  @(rf/subscribe [:username])
          userpage      (str "@" current-user)
          page-uid      (common-db/get-page-uid @db/dsdb userpage)]
      {:fx [[:dispatch [:navigate :page {:id page-uid}]]]})))

(rf/reg-event-fx
  :notification/open-in-sidebar
  (fn [_ _]
    (let [current-user @(rf/subscribe [:username])
          userpage     (str "@" current-user)]
      {:fx [[:dispatch [:right-sidebar/open-page  userpage]]]})))


(rf/reg-event-fx
  :notification/move-to-read
  (fn [{:keys [db]} [_ page-name uid]]
    (let [children   (common-db/get-children-uids @db/dsdb [:block/uid uid])
          read-block (second (common-db/get-children-uids @db/dsdb [:node/title page-name]))]
      {:fx [[:dispatch [:drop-multi/child {:source-uids children
                                           :target-uid read-block}]]]})))


(rf/reg-fx
  :discord-bot
  (fn [message]
    (println "discord bot" message)
    (go (let [response (<! (http/post "https://3txhfpivzk.execute-api.us-east-2.amazonaws.com/Prod/hello/"
                                      {:query-params message}))]
          (prn  (:body response))) ;; print the response's body in the console
        {})))

(rf/reg-sub
  :notification/to-read-count
  (fn [_ _]
    (let [userpage      (str "@" @(rf/subscribe [:username]))
          notif-count   (count (common-db/get-children-uids @db/dsdb [:block/uid (first (common-db/get-children-uids @db/dsdb [:node/title userpage]))]))]
      notif-count)))