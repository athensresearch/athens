(ns athens.views.pages.all-pages
  (:require
    ["/components/AllPagesTable/AllPagesTable" :refer [AllPagesTable]]
    ["@chakra-ui/react" :refer [Table Thead Tbody Tfoot Tr Th Td TableContainer
                                Box
                                Button
                                Stack
                                Text
                                Heading
                                Radio RadioGroup
                                SimpleGrid
                                Checkbox CheckboxGroup]]
    ["react-twitter-embed" :refer [TwitterTweetEmbed]]
    [athens.common-db          :as common-db]
    [athens.dates              :as dates]
    [athens.db                 :as db]
    [athens.router             :as router]
    [clojure.string            :refer [lower-case]]
    [re-frame.core             :as rf]
    [reagent.core :as r]))


;; Sort state and logic

(defn- get-sorted-by
  [db]
  (get db :all-pages/sorted-by :links-count))


(rf/reg-sub
  :all-pages/sorted-by
  (fn [db _]
    (get-sorted-by db)))


(rf/reg-sub
  :all-pages/sort-order-ascending?
  (fn [db _]
    (get db :all-pages/sort-order-ascending? false)))


(def sort-fn
  {:title       (fn [x] (-> x :node/title lower-case))
   :links-count (fn [x] (count (:block/_refs x)))
   :modified    :edit/time
   :created     :create/time})


(rf/reg-sub
  :all-pages/sorted
  :<- [:all-pages/sorted-by]
  :<- [:all-pages/sort-order-ascending?]
  (fn [[sorted-by growing?] [_ pages]]
    (sort-by (get sort-fn sorted-by)
             (if growing? compare (comp - compare))
             pages)))


(rf/reg-event-fx
  :all-pages/sort-by
  (fn [{:keys [db]} [_ column-id]]
    (let [sorted-column (get-sorted-by db)
          db'           (if (= column-id sorted-column)
                          (update db :all-pages/sort-order-ascending? not)
                          (-> db
                              (assoc :all-pages/sorted-by column-id)
                              (assoc :all-pages/sort-order-ascending? (= column-id :title))))]
      {:db db'
       :dispatch [:posthog/report-feature :all-pages]})))


;; types
    ;; everything
    ;; pages
    ;; blocks
    ;; daily notes
    ;; URLs
    ;; emojis
    ;; tweets
;; properties
    ;; title
    ;; string
    ;; creation time
;; view
    ;;  table
    ;;  gallery
    ;;  outliner
;;  Options


(def entity-map
  {:pages {:title "Pages"
           :query-fn (fn []
                       (->>
                        (common-db/get-all-pages @db/dsdb)
                        (filter #(not (athens.dates.is-daily-note (:block/uid %))))))}
   :daily-notes {:title "Daily Notes"
                 :query-fn (fn []
                             (->> (common-db/get-all-pages  @db/dsdb)
                                  (filter #(athens.dates.is-daily-note (:block/uid %)))))}
   :blocks {:title "Blocks"
            :query-fn  #(common-db/get-all-blocks @db/dsdb)}
   :tweets {:title "Tweets"
            :query-fn #(->> (common-db/get-all-blocks @db/dsdb)
                            (filter (fn [{:keys [block/string]} x]
                                      (re-find #"https://twitter.com/\w+/status/\d+" string))))}
   :urls {:title "URLs"
          :query-fn #() #_(->> (common-db/get-all-blocks @db/dsdb)
                          (filter (fn [{:keys [block/string]} x]
                                    (when (re-find #"" string)))))}})




;; (re-find #"\p{Emoji_Presentation}" "123")
;; (re-find #"\p{Emoji}" "👍123")
;; (re-pattern "\p{Emoji}")
;; (re-find (js/RegExp "\p{Emoji}") "👍")
;; (re-find #"foo" "FOO")




(defn page
  []
  (let [checked-types     (-> (keys entity-map)
                              (zipmap (repeat false))
                              (merge {:tweets true})
                              (r/atom))
        query-view (r/atom "gallery")]
    (fn []
      (let [count-checked (-> @checked-types vals frequencies (get true))
            all-checked             (and (pos? count-checked)
                                         (= (count @checked-types) count-checked))
            handle-click-everything (fn []
                                      (if all-checked
                                        (reset! checked-types {:pages false :blocks false :daily-notes false})
                                        (reset! checked-types {:pages true :blocks true :daily-notes true})))
            properties ["UID" "Title" "String" "Links Count" "Modified" "Created"]
            entities          (->> @checked-types
                                   (map (fn [[k v]]
                                          (if v
                                            ((get-in entity-map [k :query-fn]))
                                            [])))
                                   flatten)]

        [:> Box {:margin-top "40px"}
         #_[:h1 (str @entities)]

         [:> Box

          [:> Stack {:spacing 10 :direction "column" :padding-left 5}
           [:> Stack {:spacing 10 :direction "row" :colorScheme "blue"}
            [:> Text {:as "u" :width 100} "Entities"]
            [:> Checkbox {:is-checked all-checked :on-change handle-click-everything} "All entities"]
            (doall
             (for [[k v] entity-map]
               [:> Checkbox {:key k
                             :is-checked (k @checked-types)
                             :on-change  (fn [e] (swap! checked-types assoc k (.. e -target -checked)))}
                ( :title v )]))]

           [:> Stack {:spacing 10 :direction "row" :colorScheme "blue"}
            [:> Text {:as "u" :width 100} "Properties"]
            [:> Checkbox {} ""]]

           [:> RadioGroup {:defaultValue @query-view :on-change #(reset! query-view %)}
            [:> Stack {:spacing 10 :direction "row" :colorScheme "blue"}
             [:> Text {:as "u" :width 100} "Views"]
             [:> Radio {:value "table"} "Table"]
             [:> Radio {:value "gallery" :on-click #()} "Gallery"]
             ;; [:> Radio {:value "table"} "Board"]
             ;; [:> Radio {:value "table"} "Calendar"]
             ;; [:> Radio {:value "table"} "Outline"]
             ;; [:> Radio {:value "table"} "Graph"]
             ]]]]

         ;; how do we dynamically show propertis
         (case @query-view
           "gallery"
           [:> SimpleGrid {:columns 4 :spacing 10}
            (for [{:keys [:block/string]} entities
                  :let [tweet-id (->> string
                                      (re-find #"https://twitter.com/\w+/status/(\d+)")
                                      second)]]
              [:> TwitterTweetEmbed {:tweetId tweet-id}])]

           "table"
           [:> TableContainer {:margin-top "30px"}
            [:> Table {:variant "striped"}
             [:> Thead
              [:> Tr
               (for [p properties]
                 [:> Th {:key p} p])]]
             [:> Tbody
              (for [{:keys [:node/title :block/_refs :block/string :block/uid]
                     create-time :create/time edit-time :edit/time} entities]
                [:> Tr {:key uid}
                 [:> Td uid]
                 [:> Td [:> Text {:max-width "600px" :noOfLines 1} title]]
                 [:> Td [:> Text {:max-width "600px" :noOfLines 1} string]]
                 [:> Td (count _refs)]
                 [:> Td create-time]
                 [:> Td edit-time]]
                )]]])])))


  #_(defn page
      []
      (let [all-pages (common-db/get-all-pages @db/dsdb)]
        (fn []
          (let [sorted-pages @(rf/subscribe [:all-pages/sorted all-pages])]
            [:> AllPagesTable {:sortedPages (clj->js sorted-pages :keyword-fn str)
                               :sortedBy @(rf/subscribe [:all-pages/sorted-by])
                               :dateFormatFn #(dates/date-string %)
                               :sortDirection (if @(rf/subscribe [:all-pages/sort-order-ascending?]) "asc" "desc")
                               :onClickSort #(rf/dispatch [:all-pages/sort-by (cond
                                                                                (= % "title") :title
                                                                                (= % "links-count") :links-count
                                                                                (= % "modified") :modified
                                                                                (= % "created") :created)])
                               :onClickItem (fn [e title]
                                              (let [shift? (.-shiftKey e)]
                                                (rf/dispatch [:reporting/navigation {:source :all-pages
                                                                                     :target :page
                                                                                     :pane   (if shift?
                                                                                               :right-pane
                                                                                               :main-pane)}])
                                                (router/navigate-page title e)))}])))))
