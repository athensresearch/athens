(ns athens.bot
  (:require [cljs-http.client :as http]
            [re-frame.core :as rf]
            [athens.common-db :as common-db]
            [athens.db :as db]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def users
  {"[[@Jeff]]"        "<@!374269202103664651>"
   "[[@Alex]]"        "<@!776180002722021396>"
   "[[@Sid]]"         "<@!701431351387226143>"
   "[[@Filipe]]"      "<@!469099065712312320>"
   "[[@Stuart]]"      "<@!229346773104066562>"
   "[[@Athens Team]]" "<@&858004385215938560>"})


(defn parse-for-mentions
  [content]
  (loop [parsed content
         users users]
    (if (empty? users)
      parsed
      (recur (clojure.string/replace parsed (ffirst users) (second (first users)))
             (drop 1 users)))))


(rf/reg-event-fx
  :check-for-mentions
  (fn [_ [_ uid block-string author]]
    (let [block-parent         (:block/string (common-db/get-parent @db/dsdb [:block/uid uid]))
          is-parent-a-new-task (contains? #{"To Do 🤔" "Todo"} block-parent)
          contains-mention?    (contains?
                                 (into #{} (map
                                             (fn [[k _]] (clojure.string/includes? block-string k))
                                             users))
                                 true)]
      (cond
        (true? is-parent-a-new-task) {:fx [[:dispatch [:prepare-message uid author :new-task {:string block-string}]]]}
        (true? contains-mention?)    {:fx [[:dispatch [:prepare-message uid author :mention {:string block-string}]]]}))))


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
      {:fx [[:dispatch [:notification/send message]]]})))


(rf/reg-event-fx
  :notification/send
  (fn [_ [_ message]]
    (println "send notification" message)
    {:discord-bot message}))

(rf/reg-fx
  :discord-bot
  (fn [message]
    (println "discord bot" message)
    (go (let [response (<! (http/post "https://3txhfpivzk.execute-api.us-east-2.amazonaws.com/Prod/hello/"
                                      {:query-params message}))]
          (prn  (:body response))) ;; print the response's body in the console
        {})))
