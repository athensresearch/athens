(ns athens.views.blocks.content
  (:require
    ["/components/Block/Content" :refer [Content]]
    [athens.config :as config]
    [athens.db :as db]
    [athens.events.selection :as select-events]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.subs.selection :as select-subs]
    [athens.util :as util]
    [athens.views.blocks.internal-representation :as internal-representation]
    [athens.views.blocks.textarea-keydown :as textarea-keydown]
    [clojure.edn :as edn]
    [clojure.set :as set]
    [clojure.string :as str]
    [goog.events :as goog-events]
    [komponentit.autosize :as autosize]
    [re-frame.core :as rf])
  (:import
    (goog.events
      EventType)))


(defn find-selected-items
  "Used by both shift-click and click-drag for multi-block-selection.
  Given a mouse event, a source block, and a target block, highlight blocks.
  Find all blocks on the page using the DOM.
  Determine if direction is up or down.
  Algorithm: call select-up or select-down until start and end of vector are source and target.

  Bug: there isn't an algorithmic path for all pairs of source and target blocks, because sometimes the parent is
  highlighted, meaning a child block might not be selected itself. Rather, it inherits selection from parent.

  e.g.: 1 and 3 as source and target, or vice versa.
  • 1
  • 2
   • 3
  Because of this bug, add additional exit cases to prevent stack overflow."
  [e source-uid target-uid]
  (let [target              (.. e -target)
        page                (or (.. target (closest ".node-page"))
                                (.. target (closest ".block-page")))
        blocks              (->> (.. page (querySelectorAll ".block-container"))
                                 array-seq
                                 vec)
        uids                (map util/get-dataset-uid blocks)
        uids->children-uids (->> (zipmap uids
                                         (map util/get-dataset-children-uids blocks))
                                 (remove #(-> % second empty?))
                                 (into {}))
        indexed-uids        (map-indexed vector uids)
        start-index         (->> indexed-uids
                                 (filter (fn [[_idx uid]]
                                           (= source-uid uid)))
                                 ffirst)
        end-index           (->> indexed-uids
                                 (filter (fn [[_idx uid]]
                                           (= target-uid uid)))
                                 ffirst)
        selected-uids       (set @(rf/subscribe [::select-subs/items]))
        candidate-uids      (->> indexed-uids
                                 (filter (fn [[idx _uid]]
                                           (<= (min start-index end-index)
                                               idx
                                               (max start-index end-index))))
                                 (map second)
                                 (into #{}))
        descendants-uids    (loop [descendants    #{}
                                   ancestors-uids candidate-uids]
                              (if (seq ancestors-uids)
                                (let [ancestors-children (->> ancestors-uids
                                                              (mapcat #(get uids->children-uids %))
                                                              (into #{}))]
                                  (recur (set/union descendants ancestors-children)
                                         ancestors-children))
                                descendants))
        to-remove-uids      (set/intersection selected-uids descendants-uids)
        selection-new-uids  (set/difference candidate-uids descendants-uids)
        new-selected-uids   (-> selected-uids
                                (set/difference to-remove-uids)
                                (set/union selection-new-uids))
        selection-order     (->> indexed-uids
                                 (filter (fn [[_k v]]
                                           (contains? new-selected-uids v)))
                                 (mapv second))]
    (when config/debug?
      (js/console.debug (str "selection: " (pr-str selected-uids)
                             ", candidates: " (pr-str candidate-uids)
                             ", descendants: " (pr-str descendants-uids)
                             ", rm: " (pr-str to-remove-uids)
                             ", add: " (pr-str selection-new-uids)))
      (js/console.debug :find-selected-items (pr-str {:source-uid      source-uid
                                                      :target-uid      target-uid
                                                      :selection-order selection-order})))
    (when (and start-index end-index)
      (rf/dispatch [::select-events/set-items selection-order]))))


;; Event Handlers


(defn textarea-paste
  "Clipboard data can only be accessed if user triggers JavaScript paste event.
  Uses previous keydown event to determine if shift was held, since the paste event has no knowledge of shift key.

  Image Cases:
  - items N=1, image/png
  - items N=2, text/html and image/png
  For both of these, just write image to filesystem. Roam behavior is to copy the <img> src and alt of the copied picture.
  Roam's approach is useful to preserve the original source url and description, but is unsafe in case the link breaks.
  Writing to filesystem (or to Firebase a la Roam) is useful, but has storage costs.
  Writing to filesystem each time for now until get feedback otherwise that user doesn't want to save the image.
  Can eventually become a setting.

  Plaintext cases:
  - User pastes and last keydown has shift -> default
  - User pastes and clipboard data doesn't have new lines -> default
  - User pastes without shift and clipboard data has new line characters -> PREVENT default and convert to outliner blocks"
  [e uid state]
  (let [data                    (.. e -clipboardData)
        text-data               (.getData data "text/plain")
        ;; With internal representation
        internal-representation (some-> (.getData data "application/athens-representation")
                                        edn/read-string)
        internal?               (seq internal-representation)
        new-uids                (internal-representation/new-uids-map internal-representation)
        repr-with-new-uids      (into [] (internal-representation/update-uids internal-representation new-uids))

        ;; For images in clipboard
        items               (array-seq (.. e -clipboardData -items))
        {:keys [head tail]} (athens.views.blocks.textarea-keydown/destruct-target (.-target e))
        img-regex           #"(?i)^image/(p?jpeg|gif|png)$"
        callback            (fn [new-str]
                              (js/setTimeout #(swap! state assoc :string/local new-str)
                                             50))

        ;; External to internal representation
        text-to-inter (when-not (str/blank? text-data)
                        (internal-representation/text-to-internal-representation text-data))
        line-breaks   (re-find #"\r?\n" text-data)
        no-shift      (-> @state :last-keydown :shift not)]


    (cond
      ;; For internal representation
      internal?
      (do
        (.. e preventDefault)
        (rf/dispatch [:paste-internal uid (:string/local @state) repr-with-new-uids]))

      ;; For images
      (seq (filter (fn [item]
                     (let [datatype (.. item -type)]
                       (re-find img-regex datatype))) items))
      ;; Need dispatch-sync because with dispatch we lose the clipboard data context
      ;; on callee side
      (rf/dispatch-sync [:paste-image items head tail callback])

      ;; For external copy-paste
      (and line-breaks no-shift)
      (do
        (.. e preventDefault)
        (rf/dispatch [:paste-internal uid (:string/local @state) text-to-inter]))

      (not no-shift)
      (do
        (.. e preventDefault)
        (rf/dispatch [:paste-verbatim uid text-data]))

      :else
      nil)))


(defn textarea-change
  [e _uid state]
  (swap! state assoc :string/local (.. e -target -value))
  ((:string/idle-fn @state)))


(defn textarea-click
  "If shift key is held when user clicks across multiple blocks, select the blocks."
  [e target-uid _state]
  (let [[target-uid _] (db/uid-and-embed-id target-uid)
        source-uid     @(rf/subscribe [:editing/uid])
        shift?         (.-shiftKey e)]
    (if (and shift?
             source-uid
             target-uid
             (not= source-uid target-uid))
      (find-selected-items e source-uid target-uid)
      (rf/dispatch [::select-events/clear]))))


(defn global-mouseup
  "Detach global mouseup listener (self)."
  [_]
  (goog-events/unlisten js/document EventType.MOUSEUP global-mouseup)
  (rf/dispatch [:mouse-down/unset]))


(defn textarea-mouse-down
  "Attach global mouseup listener. Listener can't be local because user might let go of mousedown off of a block.
  See https://javascript.info/mouse-events-basics#events-order"
  [e _uid _]
  (.. e stopPropagation)
  (when (false? (.. e -shiftKey))
    (rf/dispatch [:editing/target (.. e -target)])
    (let [mouse-down @(rf/subscribe [:mouse-down])]
      (when (false? mouse-down)
        (rf/dispatch [:mouse-down/set])
        (goog-events/listen js/document EventType.MOUSEUP global-mouseup)))))


(defn textarea-mouse-enter
  "When mouse-down, user is selecting multiple blocks with click+drag.
  Use same algorithm as shift-enter, only updating the source and target."
  [e target-uid _]
  (let [source-uid @(rf/subscribe [:editing/uid])
        mouse-down @(rf/subscribe [:mouse-down])]
    (when mouse-down
      (rf/dispatch [::select-events/clear])
      (find-selected-items e source-uid target-uid))))


;; View

(defn block-content-el
  "Actual string contents. Two elements, one for reading and one for writing.
  The CSS class is-editing is used for many things, such as block selection.
  Opacity is 0 when block is selected, so that the block is entirely blue, rather than darkened like normal editing.
  is-editing can be used for shift up/down, so it is used in both editing and selection."
  [block state]
  (let [{:block/keys [uid original-uid header]} block
        editing? (rf/subscribe [:editing/is-editing uid])
        selected-items (rf/subscribe [::select-subs/items])]
    (fn [_block _state]
      (let [font-size (case header
                        1 "2.1em"
                        2 "1.7em"
                        3 "1.3em"
                        "1em")]
        [:> Content {:fontSize font-size
                     :on-click  (fn [e] (.. e stopPropagation) (rf/dispatch [:editing/uid uid]))}
         ;; NOTE: komponentit forces reflow, likely a performance bottle neck
         ;; When block is in editing mode or the editing DOM elements are rendered
         (when (or (:show-editable-dom @state) @editing?)
           [autosize/textarea {:value          (:string/local @state)
                               :class          ["block-input-textarea" (when (and (empty? @selected-items) @editing?) "is-editing")]
                               ;; :auto-focus  true
                               :id             (str "editable-uid-" uid)
                               :on-change      (fn [e] (textarea-change e uid state))
                               :on-paste       (fn [e] (textarea-paste e uid state))
                               :on-key-down    (fn [e] (textarea-keydown/textarea-key-down e uid state))
                               :on-blur        (:string/save-fn @state)
                               :on-click       (fn [e] (textarea-click e uid state))
                               :on-mouse-enter (fn [e] (textarea-mouse-enter e uid state))
                               :on-mouse-down  (fn [e] (textarea-mouse-down e uid state))}])
         ;; TODO pass `state` to parse-and-render
         [parse-and-render (:string/local @state) (or original-uid uid)]]))))

