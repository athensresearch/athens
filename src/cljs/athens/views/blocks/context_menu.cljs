(ns athens.views.blocks.context-menu
  (:require
    ["/components/Button/Button" :refer [Button]]
    [athens.bot :as bot]
    [athens.db :as db]
    [athens.listeners :as listeners]
    [athens.subs.selection :as select-subs]
    [athens.views.dropdown :refer [menu-style dropdown-style]]
    [clojure.string :as string]
    [goog.events :as events]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [stylefy.core :as stylefy]
    [athens.common-db :as common-db]))


(defn copy-refs-mouse-down
  [_ uid state]
  (let [selected-items @(rf/subscribe [::select-subs/items])
        ;; use this when using datascript-transit
        ;; uids (map (fn [x] [:block/uid x]) selected-items)
        ;; blocks (d/pull-many @db/dsdb '[*] ids)
        data           (if (empty? selected-items)
                         (str "((" uid "))")
                         (->> (map (fn [uid] (str "((" uid "))\n")) selected-items)
                              (string/join "")))]
    (.. js/navigator -clipboard (writeText data))
    (swap! state assoc :context-menu/show false)))


(defn bullet-context-menu
  "Handle right click. If no blocks are selected, just give option for copying current block's uid."
  [e _uid state]
  (.. e preventDefault)
  (let [rect (.. e -target getBoundingClientRect)]
    (swap! state assoc
           :context-menu/x (.. rect -left)
           :context-menu/y (.. rect -bottom)
           :context-menu/show true)))


(defn handle-copy-unformatted
  "If copying only a single block, dissoc children to not copy subtree."
  [^js e uid state]
  (let [uids @(rf/subscribe [::select-subs/items])]
    (if (empty? uids)
      (let [block (dissoc (db/get-block [:block/uid uid]) :block/children)
            data  (listeners/blocks-to-clipboard-data 0 block true)]
        (.. js/navigator -clipboard (writeText data)))
      (let [data (->> (map #(db/get-block [:block/uid %]) uids)
                      (map #(listeners/blocks-to-clipboard-data 0 % true))
                      (apply str))]
        (.. js/navigator -clipboard (writeText data)))))
  (.. e preventDefault)
  (swap! state assoc :context-menu/show false))

(defn handle-click-comment
  [e uid state]
  (re-frame.core/dispatch [:comment/show-comment-textarea uid])
  (.. e preventDefault)
  (swap! state assoc :context-menu/show false))

(defn handle-move-notifications
  [e uid state page-name]
  (rf/dispatch [:notification/move-to-read page-name uid])
  (.. e preventDefault)
  (swap! state assoc :context-menu/show false))

(defn context-menu-el
  "Only option in context menu right now is copy block ref(s)."
  [_block state]
  (let [ref                  (atom nil)
        handle-click-outside (fn [e]
                               (when (and (:context-menu/show @state)
                                          (not (.. @ref (contains (.. e -target)))))
                                 (swap! state assoc :context-menu/show false)))
        current              @(rf/subscribe [:current-route/name])
        current-page         @(rf/subscribe [:current-route/page-title])]
    (r/create-class
      {:display-name           "context-menu"
       :component-did-mount    (fn [_this] (events/listen js/document "mousedown" handle-click-outside))
       :component-will-unmount (fn [_this] (events/unlisten js/document "mousedown" handle-click-outside))
       :reagent-render         (fn [block state]
                                 (let [{:block/keys [uid]} block
                                       {:context-menu/keys [x y show]} @state
                                       selected-items @(rf/subscribe [::select-subs/items])]
                                   (when show
                                     [:div (merge (stylefy/use-style dropdown-style
                                                                     {:ref #(reset! ref %)})
                                                  {:style {:position "fixed"
                                                           :left     (str x "px")
                                                           :top      (str y "px")}})
                                      [:div (stylefy/use-style menu-style)
                                       [:> Button {:on-mouse-down (fn [e] (copy-refs-mouse-down e uid state))}
                                        (if (empty? selected-items)
                                          "Copy block ref"
                                          "Copy block refs")]
                                       [:> Button {:on-mouse-down (fn [e] (handle-copy-unformatted e uid state))}
                                        "Copy unformatted"]
                                       (when (empty? selected-items)
                                         [:> Button {:on-mouse-down (fn [e] (handle-click-comment e uid state))}
                                          "Comment"])
                                       (when (some #{current-page} bot/athens-users )
                                         [:> Button {:on-mouse-down (fn [e] (handle-move-notifications e uid state current-page))}
                                          "Move notifications to read"])]])))})))
