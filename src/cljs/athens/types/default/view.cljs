(ns athens.types.default.view
  "Default Block Type Renderer.
  A.k.a standard `:block/string` blocks"
  (:require
    ["/components/Icons/Icons"  :refer [PencilIcon]]
    ["@chakra-ui/react"         :refer [Box Button ButtonGroup IconButton]]
    [athens.common-db           :as common-db]
    [athens.common.utils        :as utils]
    [athens.db                  :as    db]
    [athens.parse-renderer      :as    parser]
    [athens.reactive            :as    reactive]
    [athens.router              :as    router]
    [athens.types.core          :as    types]
    [athens.types.dispatcher    :as    dispatcher]
    [athens.util                :as    util]
    [athens.views.blocks.editor :as    editor]
    [clojure.string             :as    str]
    [goog.functions             :as    gfns]
    [re-frame.core              :as    rf]
    [reagent.core               :as    r]
    [reagent.ratom              :as    ratom]))


(defn- block-breadcrumb-string
  [parents]
  (->> parents
       (map #(or (:node/title %)
                 (:block/string %)))
       (str/join " >\n")))


(defn zoomed-in-view-el
  [_this block-data callbacks]
  (let [{:block/keys [uid
                      original-uid
                      string]}       block-data
        local-value                  (r/atom string)
        show-edit-atom?              (r/atom true)
        savep-fn                     (partial db/transact-state-for-uid (or original-uid uid))
        save-fn                      #(savep-fn @local-value :block-save)
        idle-fn                      (gfns/debounce #(savep-fn @local-value :autosave)
                                                    2000)
        update-fn                    #(reset! local-value %)
        enter-handler                (fn [uid d-key-down]
                                       (let [[uid embed-id]         (common-db/uid-and-embed-id uid)
                                             new-uid               (utils/gen-block-uid)
                                             {:keys [start value]}  d-key-down]
                                         (rf/dispatch [:enter/split-block {:uid uid
                                                                           :value             value
                                                                           :index             start
                                                                           :new-uid           new-uid
                                                                           :embed-id          embed-id
                                                                           :relation          :first}])))
        state-hooks                  (merge callbacks
                                            {:save-fn        save-fn
                                             :idle-fn        idle-fn
                                             :update-fn      update-fn
                                             :show-edit?     show-edit-atom?
                                             :read-value     local-value
                                             :enter-handler  enter-handler})]
    [editor/block-editor block-data state-hooks]))


(defn inline-ref-view-el
  [_this block {:keys [from title]} ref-uid uid _callback _with-breadcrumb?]
  (let [parents   (reactive/get-reactive-parents-recursively [:block/uid ref-uid])
        bc-string (block-breadcrumb-string parents)]
    (if block
      [:> Button {:variant           "link"
                  :as                "a"
                  :title             (-> from
                                         (str/replace "]("
                                                      "]\n---\n(")
                                         (str/replace (str "((" ref-uid "))")
                                                      bc-string))
                  :class             "block-ref"
                  :display           "inline"
                  :color             "unset"
                  :whiteSpace        "unset"
                  :textAlign         "unset"
                  :minWidth          "0"
                  :fontSize          "inherit"
                  :fontWeight        "inherit"
                  :lineHeight        "inherit"
                  :marginInline      "-2px"
                  :paddingInline     "2px"
                  :borderBottomWidth "1px"
                  :borderBottomStyle "solid"
                  :borderBottomColor "ref.foreground"
                  :cursor            "alias"
                  :sx                {"WebkitBoxDecorationBreak" "clone"
                                      :h1 {:marginBlock 0
                                           "&:not(:last-child)" {:paddingInlineEnd "0.35ch"}
                                           :fontSize "inherit"
                                           :display "inline-block"}
                                      :h2 {:marginBlock 0
                                           "&:not(:last-child)" {:paddingInlineEnd "0.35ch"}
                                           :fontSize "inherit"
                                           :display "inline-block"}
                                      :h3 {:marginBlock 0
                                           "&:not(:last-child)" {:paddingInlineEnd "0.35ch"}
                                           :fontSize "inherit"
                                           :display "inline-block"}
                                      :h4 {:marginBlock 0
                                           "&:not(:last-child)" {:paddingInlineEnd "0.35ch"}
                                           :fontSize "inherit"
                                           :display "inline-block"}
                                      :h5 {:marginBlock 0
                                           "&:not(:last-child)" {:paddingInlineEnd "0.35ch"}
                                           :fontSize "inherit"
                                           :display "inline-block"}
                                      :h6 {:marginBlock 0
                                           "&:not(:last-child)" {:paddingInlineEnd "0.35ch"}
                                           :fontSize "inherit"
                                           :display "inline-block"}
                                      :p {:display "inline-block"
                                          :marginBlock 0}}
                  :_hover            {:textDecoration    "none"
                                      :borderBottomColor "transparent"
                                      :bg                "ref.background"}
                  :onClick           (fn [e]
                                       (.. e stopPropagation)
                                       (let [shift? (.-shiftKey e)]
                                         (rf/dispatch [:reporting/navigation {:source :pr-block-ref
                                                                              :target :block
                                                                              :pane   (if shift?
                                                                                        :right-pane
                                                                                        :main-pane)}])
                                         (router/navigate-uid ref-uid e)))}
       (cond
         (= uid ref-uid)
         [parser/parse-and-render "{{SELF}}"]

         (not (str/blank? title))
         [parser/parse-and-render title ref-uid]

         :else
         [parser/parse-and-render (:block/string block) ref-uid])]
      from)))


(defn outline-view-el
  [_this block-data callbacks]
  (let [{:block/keys [uid
                      original-uid]} block-data
        local-value                  (r/atom nil)
        old-value                    (r/atom nil)
        savep-fn                     (partial db/transact-state-for-uid (or original-uid uid))
        save-fn                      #(savep-fn @local-value :block-save)
        idle-fn                      (gfns/debounce #(savep-fn @local-value :autosave)
                                                    2000)
        update-fn                    #(reset! local-value %)
        update-old-fn                #(reset! old-value %)
        read-value                   (ratom/reaction @local-value)
        read-old-value               (ratom/reaction @old-value)
        state-hooks                  (merge callbacks
                                            {:save-fn        save-fn
                                             :idle-fn        idle-fn
                                             :update-fn      update-fn
                                             :update-old-fn  update-old-fn
                                             :read-value     read-value
                                             :read-old-value read-old-value})]
    (fn render-block
      [_this block _callbacks]
      (let [ident                 [:block/uid (or original-uid uid)]
            block-o               (reactive/get-reactive-block-document ident)
            {:block/keys [string
                          _refs]} (merge block-o block)]

        ;; (prn uid is-selected)

        ;; If datascript string value does not equal local value, overwrite local value.
        ;; Write on initialization
        ;; Write also from backspace, which can join bottom block's contents to top the block.
        (update-fn string)

        [editor/block-editor block state-hooks]))))


(defn tranclusion-view-el
  [this block-el block-uid {:keys [transcluding-block-uid] :as _config} transclusion-scope]
  (let [supported-trans (types/supported-transclusion-scopes this)]
    (if-not (contains? supported-trans transclusion-scope)
      (throw (ex-info (str "Invalid transclusion scope: " (pr-str transclusion-scope)
                           ". Supported transclusion types: " (pr-str supported-trans))
                      {:supported-transclusion-scopes supported-trans
                       :provided-transclusion-scope   transclusion-scope}))
      (let [embed-id (random-uuid)
            block    (reactive/get-reactive-block-document [:block/uid block-uid])]
        [:> Box {:class    "block-embed"
                 :bg       "background.basement"
                 :flex     1
                 :pr       1
                 :position "relative"
                 :display  "flex"
                 :sx       {"> .block-container" {:ml        0
                                                  :flex      1
                                                  :pr        "1.3rem"
                                                  "textarea" {:background "transparent"}}}}
         [:<>
          [:f> block-el
           (util/recursively-modify-block-for-embed block embed-id)
           {:linked-ref false}
           {:block-embed? true}]
          (when-not @(rf/subscribe [:editing/is-editing block-uid])
            [:> ButtonGroup {:height "2em" :size "xs" :flex "0 0 auto" :zIndex "5" :alignItems "center"}
             [:> IconButton {:on-click (fn [e]
                                         (.. e stopPropagation)
                                         (rf/dispatch [:editing/uid transcluding-block-uid]))}
              [:> PencilIcon]]])]]))))


(defrecord DefaultBlockRenderer
  [linked-ref-data]

  types/BlockTypeProtocol

  (text-view
    [_this {:block/keys [string]} {:keys [_from title]}]
    (if (not (str/blank? title))
      (parser/parse-to-text title)
      (parser/parse-to-text string)))


  (inline-ref-view
    [_this block attr ref-uid uid _callback _with-breadcrumb?]
    [inline-ref-view-el _this block attr ref-uid uid _callback _with-breadcrumb?])


  (outline-view
    [_this block-data callbacks]
    [outline-view-el _this block-data callbacks])


  (supported-transclusion-scopes
    [_this]
    #{:embed})


  (transclusion-view
    [this block-el block-uid callback transclusion-scope]
    [tranclusion-view-el this block-el block-uid callback transclusion-scope])


  (zoomed-in-view
    [_this block-data callbacks]
    [zoomed-in-view-el _this block-data callbacks])


  (supported-breadcrumb-styles
    [_this]
    #{:string})


  (breadcrumbs-view
    [_this _block-data _callbacks _breadcrumb-style]))


(defmethod dispatcher/block-type->protocol :default [_k args-map]
  (DefaultBlockRenderer. (:linked-ref-data args-map)))
