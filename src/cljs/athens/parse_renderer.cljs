^:cljstyle/ignore
(ns athens.parse-renderer
  (:require
    ["katex" :as katex]
    ["katex/dist/contrib/mhchem"]
    #_["codemirror/addon/edit/closebrackets"]
    #_["codemirror/addon/edit/matchbrackets"]
    #_["codemirror/mode/clike/clike"]
    #_["codemirror/mode/clojure/clojure"]
    #_["codemirror/mode/coffeescript/coffeescript"]
    #_["codemirror/mode/commonlisp/commonlisp"]
    #_["codemirror/mode/css/css"]
    #_["codemirror/mode/dart/dart"]
    #_["codemirror/mode/dockerfile/dockerfile"]
    #_["codemirror/mode/elm/elm"]
    #_["codemirror/mode/erlang/erlang"]
    #_["codemirror/mode/go/go"]
    #_["codemirror/mode/haskell/haskell"]
    #_["codemirror/mode/javascript/javascript"]
    #_["codemirror/mode/lua/lua"]
    #_["codemirror/mode/mathematica/mathematica"]
    #_["codemirror/mode/perl/perl"]
    #_["codemirror/mode/php/php"]
    #_["codemirror/mode/powershell/powershell"]
    #_["codemirror/mode/python/python"]
    #_["codemirror/mode/ruby/ruby"]
    #_["codemirror/mode/rust/rust"]
    #_["codemirror/mode/scheme/scheme"]
    #_["codemirror/mode/shell/shell"]
    #_["codemirror/mode/smalltalk/smalltalk"]
    #_["codemirror/mode/sql/sql"]
    #_["codemirror/mode/swift/swift"]
    #_["codemirror/mode/vue/vue"]
    #_["codemirror/mode/xml/xml"]
    #_["react-codemirror2" :rename {UnControlled CodeMirror}]
    [athens.config :as config]
    [athens.db :as db]
    [athens.parser.impl :as parser-impl]
    [athens.router :refer [navigate-uid]]
    [athens.style :refer [color OPACITIES]]
    [clojure.string :as str]
    [instaparse.core :as insta]
    [posh.reagent :refer [pull #_q]]
    [stylefy.core :as stylefy :refer [use-style]]))


(declare parse-and-render)


;;; Styles

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


(def hashtag
  {::stylefy/mode [[:hover {:text-decoration "underline" :cursor "pointer"}]]
   ::stylefy/manual [[:.formatting {:opacity (:opacity-low OPACITIES)}]]})


(def image {:border-radius "0.125rem"})


(def url-link
  {:cursor "pointer"
   :text-decoration "none"
   :color (color :link-color)
   ::stylefy/manual [[:.formatting {:color (color :body-text-color :opacity-low)}]
                     [:&:hover {:text-decoration "underline"}]]})


(def autolink
  {:cursor "pointer"
   :text-decoration "none"
   ::stylefy/manual [[:.formatting {:color (color :body-text-color :opacity-low)}]
                     [:.contents {:color (color :link-color)
                                  :text-decoration "none"}]
                     [:&:hover [:.contents {:text-decoration "underline"}]]]})


(def block-ref
  {:font-size "0.9em"
   :transition "background 0.05s ease"
   :border-bottom [["1px" "solid" (color :highlight-color)]]
   ::stylefy/mode [[:hover {:background-color (color :highlight-color :opacity-lower)
                            :cursor "alias"}]]})


(defn parse-title
  "Title coll is a sequence of plain strings or hiccup elements. If string, return string, otherwise parse the hiccup
  for its plain-text representation."
  [title-coll]
  (->> (map (fn [el]
              (if (string? el)
                el
                (str "[[" (clojure.string/join (get-in el [3 2])) "]]"))) title-coll)
       (str/join "")))


;;; Helper functions for recursive link rendering
(defn pull-node-from-string
  "Gets a block's node from the display string name (or partially parsed string tree)"
  [title-coll]
  (let [title (parse-title title-coll)]
    (pull db/dsdb '[*] [:node/title title])))


(defn render-page-link
  "Renders a page link given the title of the page."
  [title]
  (let [node (pull-node-from-string title)]
    [:span (use-style page-link {:class "page-link"})
     [:span {:class "formatting"} "[["]
     (into [:span {:on-click (fn [e]
                               (.. e stopPropagation) ;; prevent bubbling up click handler for nested links
                               (navigate-uid (:block/uid @node) e))}]
           title)
     [:span {:class "formatting"} "]]"]]))

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


;;; Components


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
     :page-link            (fn [& title-coll] (render-page-link title-coll))
     :hashtag              (fn [& title-coll]
                             (let [node (pull-node-from-string title-coll)]
                               [:span (use-style hashtag {:class    "hashtag"
                                                          :on-click #(navigate-uid (:block/uid @node) %)})
                                [:span {:class "formatting"} "#"]
                                [:span {:class "contents"} title-coll]]))
     :block-ref            (fn [ref-uid]
                             (let [block (pull db/dsdb '[*] [:block/uid ref-uid])]
                               (if @block
                                 [:span (use-style block-ref {:class "block-ref"})
                                  [:span {:class "contents" :on-click #(navigate-uid ref-uid %)}
                                   (if (= uid ref-uid)
                                     [parse-and-render "{{SELF}}"]
                                     [parse-and-render (:block/string @block) ref-uid])]]
                                 (str "((" ref-uid "))"))))
     :url-image            (fn [{url :src alt :alt}]
                             [:img (use-style image {:class "url-image"
                                                     :alt   alt
                                                     :src   url})])
     :url-link             (fn [{url :url} text]
                             [:a (use-style url-link {:class  "url-link"
                                                      :href   url
                                                      :target "_blank"})
                              text])
     :link                 (fn [{:keys [text target title]}]
                             [:a (cond-> (use-style url-link {:class  "url-link contents"
                                                              :href target
                                                              :target "_blank"})
                                   (string? title)
                                   (assoc :title title))
                              text])
     :autolink             (fn [{:keys [text target]}]
                             [:span (use-style autolink)
                              [:span {:class "formatting"} "<"]
                              [:a {:class  "autolink contents"
                                   :href target
                                   :target "_blank"}
                               text]
                              [:span {:class "formatting"} ">"]])
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
     :indented-code-block (fn [code-text]
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
