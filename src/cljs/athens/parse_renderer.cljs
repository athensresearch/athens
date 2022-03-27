^:cljstyle/ignore
(ns athens.parse-renderer
  (:require
   ["@chakra-ui/react" :refer [Link Button Text]]
   ["katex" :as katex]
   ["katex/dist/contrib/mhchem"]
   [athens.config :as config]
   [athens.parser.impl :as parser-impl]
   [athens.reactive :as reactive]
   [athens.router :as router]
   [athens.style :refer [color OPACITIES]]
   [clojure.string :as str]
   [instaparse.core :as insta]
   [re-frame.core :as rf]
   [stylefy.core :as stylefy :refer [use-style]]))


(declare parse-and-render)

(def fm-props {:as "b" :class "formatting" :fontWeight "normal" :opacity "0.3"})

(def content-props {:as "span" :fontWeight "normal" :opacity "0.3"})

(def link-props {:color "link"
                 :variant "link"
                 :minWidth "unset"
                 :whiteSpace "normal"
                 :wordBreak "break-word"
                 :lineHeight "unset"
                 :fontSize "inherit"
                 :fontWeight "inherit"
                 :textDecoration "none"
                 :_hover {:textDecoration "none"}})

(def ref-props {:as "span"
                :white-space "normal"
                :word-break "break-word"
                :fontSize "0.9em"
                :transition "background 0.05s ease"
                :borderBottomWidth "1px"
                :borderBottomStyle "solid"
                :borderBottomColor "highlight"
                :_hover {:background "background.upper"
                         :cursor "alias"}})


;; Styles

(def page-link
  {:cursor "pointer"
   :text-decoration "none"
   :color (color :link-color)
   :display "inline"
   :border-radius "0.25rem"
   ::stylefy/manual [[:.formatting {:color (color :body-text-color)
                                    :opacity (:opacity-low OPACITIES)}]
                     [:&:hover {:z-index 1
                                :background (color :link-color :opacity-lower)
                                :box-shadow (str "0px 0px 0px 1px " (color :link-color :opacity-lower))}]]})


(def image {:border-radius "0.125rem"})


(defn parse-title
  "Title coll is a sequence of plain strings or hiccup elements. If string, return string, otherwise parse the hiccup
  for its plain-text representation."
  [title-coll]
  (->> (map (fn [el]
              (if (string? el)
                el
                (str "[[" (str/join (get-in el [3 2])) "]]"))) title-coll)
       (str/join "")))


(defn render-page-link
  "Renders a page link given the title of the page."
  [{:keys [from title]} title-coll]
  [:<>
   [:> Text fm-props "[["]
   (cond
     (not (str/blank? title))
     [:> Button
      (merge link-props
             {:class "page-link"
              :fontWeight "normal"
              :title from
              :onClick (fn [e]
                         (let [parsed-title (parse-title title-coll)
                               shift?       (.-shiftKey e)]
                           (.. e stopPropagation) ; prevent bubbling up click handler for nested links
                           (rf/dispatch [:reporting/navigation {:source :pr-page-link
                                                                :target :page
                                                                :pane   (if shift?
                                                                          :right-pane
                                                                          :main-pane)}])
                           (router/navigate-page parsed-title e)))})
      title]

     :else
     (into
      [:> Button
       (merge link-props
              {:class "page-link"
               :title from
               :onClick (fn [e]
                          (let [parsed-title (parse-title title-coll)
                                shift?       (.-shiftKey e)]
                            (.. e stopPropagation) ; prevent bubbling up click handler for nested links
                            (rf/dispatch [:reporting/navigation {:source :pr-page-link
                                                                 :target :page
                                                                 :pane   (if shift?
                                                                           :right-pane
                                                                           :main-pane)}])
                            (router/navigate-page parsed-title e)))})]
      title-coll))
   [:> Text fm-props "]]"]])


(defn- block-breadcrumb-string
  [parents]
  (->> parents
       (map #(or (:node/title %)
                 (:block/string %)))
       (str/join " >\n")))


(defn render-block-ref
  [{:keys [from title]} ref-uid uid]
  (let [block     (reactive/get-reactive-block-or-page-by-uid ref-uid)
        parents   (reactive/get-reactive-parents-recursively [:block/uid ref-uid]) 
        bc-string (block-breadcrumb-string parents)]
    (if block
       [:> Button {:variant "link"
                   :as "a"
                   :title (-> from
                              (str/replace "]("
                                           "]\n---\n(")
                              (str/replace (str "((" ref-uid "))")
                                           bc-string))
                   :class "block-ref"
                   :display "inline"
                   :color "unset"
                   :whiteSpace "unset"
                   :textAlign "unset"
                   :minWidth "unset"
                   :fontWeight "inherit"
                   :lineHeight "inherit"
                   :background "ref.feature"
                   :cursor "alias"
                   :sx {"-webkit-box-decoration-break" "clone"}
                   :_hover {:textDecoration "none"}
                   :onClick (fn [e]
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
          [parse-and-render "{{SELF}}"]

          (not (str/blank? title))
          [parse-and-render title ref-uid]

          :else
          [parse-and-render (:block/string block) ref-uid])]
      from)))


;; -- Component ---

(def components
  {#"\[\[TODO\]\]"                :todo
   #"\[\[DONE\]\]"                :done
   #"\[\[youtube\]\]\:.*"         :youtube
   #"iframe\:.*"                  :iframe
   #"SELF"                        :self
   #"\[\[embed\]\]: \(\(.+\)\)"   :block-embed})


(defmulti component
  (fn [content _uid]
    (some (fn [[pattern render]]
            (when (re-matches pattern content)
              render))
          components)))


(defmethod component :default
  [content _]
  [:button content])


;; Components


(defn- clean-single-p-appending
  [parent & contents]
  (if (and (= 1 (count contents))
           (= :p (ffirst contents)))
    (let [rest-of-p (-> contents first rest)]
      (apply conj parent rest-of-p))
    (apply conj parent contents)))


;; Instaparse transforming docs: https://github.com/Engelberg/instaparse#transforming-the-tree
(defn transform
  "Transforms Instaparse output to Hiccup."
  [tree uid]
  (insta/transform
   {:block   (fn [& contents]
               (apply clean-single-p-appending
                      [:span {:class "block"}]
                      contents))
    :heading (fn [{n :n} & contents]
               (apply clean-single-p-appending
                      [({1 :h1
                         2 :h2
                         3 :h3
                         4 :h4
                         5 :h5
                         6 :h6} n)]
                      contents))

     ;; for more information regarding how custom components are parsed, see
     ;; https://athensresearch.gitbook.io/handbook/athens/athens-components-documentation/
    :component            (fn [& contents]
                            (component (first contents) uid))
    :page-link            (fn [{_from :from :as attr} & title-coll]
                            (render-page-link attr title-coll))
    :hashtag              (fn [{_from :from} & title-coll]
                            [:> Button {:variant "link"
                                        :class   "hashtag"
                                        :color  "inherit"
                                        :fontWeight "inherit"
                                        :_hover {:textDecoration "none"}
                                        :onClick (fn [e]
                                                   (let [parsed-title (parse-title title-coll)
                                                         shift?       (.-shiftKey e)]
                                                     (rf/dispatch [:reporting/navigation {:source :pr-hashtag
                                                                                          :target :hashtag
                                                                                          :pane   (if shift?
                                                                                                    :right-pane
                                                                                                    :main-pane)}])
                                                     (router/navigate-page parsed-title e)))}
                             [:> Text fm-props "#"]
                             [:span {:class "contents"} title-coll]])
    :block-ref            (fn [{_from :from :as attr} ref-uid]
                            (render-block-ref attr ref-uid uid))
    :url-image            (fn [{url :src alt :alt}]
                            [:img (use-style image {:class "url-image"
                                                    :alt   alt
                                                    :src   url})])
    :url-link             (fn [{url :url} text]
                            [:> Button
                             (merge link-props {:class  "url-link"
                                                :href   url
                                                :target "_blank"})
                             text])
    :link                 (fn [{:keys [text target title]}]
                            [:a (cond-> (merge link-props
                                               {:class  "url-link contents"
                                                :href target
                                                :target "_blank"})
                                  (string? title)
                                  (assoc :title title))
                             text])
    :autolink             (fn [{:keys [text target]}]
                            [:<>
                             [:> Text fm-props "<"]
                             [:> Link (merge
                                       link-props
                                       {:class  "autolink contents"
                                        :href target
                                        :target "_blank"})
                              text]
                             [:> Text fm-props ">"]])
    :text-run              (fn [& contents]
                             (apply conj [:span {:class "text-run"}] contents))
    :paragraph            (fn [& contents]
                            (apply conj [:p] contents))
    :bold                 (fn [& contents]
                            (apply conj [:strong {:class "contents bold"}] contents))
    :italic               (fn [& contents]
                            (apply conj [:i {:class "contents italic"}] contents))
    :strikethrough        (fn [& contents]
                            (apply conj  [:del {:class "contents del"}] contents))
    :underline            (fn [& contents]
                            (apply conj  [:u {:class "contents underline"}] contents))
    :highlight            (fn [& contents]
                            (apply conj [:mark {:class "contents highlight"}] contents))
    :pre-formatted        (fn [text]
                            [:code text])
    :inline-pre-formatted (fn [text]
                            [:code text])
    :indented-code-block (fn [{:keys [_from]} code-text]
                           (let [text (second code-text)]
                             [:pre
                              [:code text]]))
    :fenced-code-block    (fn [{lang :lang} code-text]
                            (let [mode        (or lang "javascript")
                                  text        (second code-text)]
                              (when config/debug?
                                (js/console.log "Block code, original-mode:" lang
                                                ", mode:" mode
                                                ", text:" text))
                              [:pre
                               [:code text]]
                               ;; TODO: Followup issue: #989 "Integrate with CodeMirror for code blocks"
                              #_[:> CodeMirror {:value     text
                                                :options   {:mode              mode
                                                            :lineNumbers       true
                                                            :matchBrackets     true
                                                            :autoCloseBrackets true
                                                            :extraKeys         #js {"Esc" (fn [editor]
                                                                                          ;; TODO: save when needed
                                                                                            (js/console.log "[Esc]")
                                                                                            (if (= text @local-value)
                                                                                              (js/console.log "[Esc] no changes")
                                                                                              (do
                                                                                              ;; TODO Save
                                                                                                )))}}
                                                :on-change (fn [editor data value]
                                                             (js/console.log "on-change" editor (pr-str data) (pr-str value))
                                                             (when-not (= @local-value value)
                                                               (js/console.log "on-change, updating local state" value)
                                                               (reset! local-value value)))
                                                :on-blur   (fn [editor event]
                                                             (js/console.log "on-blur")
                                                             (if (= text @local-value)
                                                               (js/console.log "on-blur, content not modified")
                                                               (do
                                                                 (js/console.log "on-blur, content modified"
                                                                                 (pr-str text)
                                                                                 "=>"
                                                                                 (pr-str @local-value))
                                                               ;; update value based on `uid`
                                                                 )))}]))

    :latex (fn [text]
             [:span {:ref (fn [el]
                            (when el
                              (try
                                (katex/render text el (clj->js
                                                       {:throwOnError false}))
                                (catch :default e
                                  (js/console.warn "Unexpected KaTeX error" e)
                                  (aset el "innerHTML" text)))))}])
    :newline (fn [_]
               [:br])}
   tree))


(defn parse-and-render
  "Converts a string of block syntax to Hiccup, with fallback formatting if it can’t be parsed."
  [string uid]
  (when config/measure-parser?
    (js/console.group string))
  (let [pt-n-1     (js/performance.now)
        result     (parser-impl/staged-parser->ast string)
        pt-n-2     (js/performance.now)
        pt-n-total (- pt-n-2 pt-n-1)]
    (when config/measure-parser?
      (js/console.log "parsing time:" pt-n-total))
    (if (insta/failure? result)
      (do
        (when config/measure-parser?
          (js/console.groupEnd))
        [:abbr {:title (pr-str (insta/get-failure result))
                :style {:color "red"}}
         string])
      (let [vt-1     (js/performance.now)
            view     (transform result uid)
            vt-2     (js/performance.now)
            vt-total (- vt-2 vt-1)]
        (when config/measure-parser?
          (js/console.log "view creation:" vt-total)
          (js/console.groupEnd))
        view))))
