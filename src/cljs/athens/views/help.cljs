(ns athens.views.help
  (:require
    ["@chakra-ui/react" :refer [Text Heading Box Modal ModalOverlay ModalContent ModalHeader ModalBody ModalCloseButton]]
    [athens.util :as util]
    [clojure.string :as str]
    [re-frame.core :refer [dispatch subscribe]]
    [reagent.core :as r]))


;; Helpers to create the help content
;; ==========================
(defn faded-text
  [text]
  [:> Text {:as "span"
            :color "foreground.secondary"
            :fontWeight "normal"}
   text])


(defn space
  []
  [:> Box  {:as "i"
            :width "0.5em"
            :display "inline-block"
            :marginInline "0.125em"
            :background "currentColor"
            :height "1px"
            :opacity "0.5"}])


(defn- add-keys
  [sequence]
  (doall
    (for [el sequence]
      ^{:keys (hash el)}
      el)))


(defn example
  [template & args]
  (let [faded-texts    (map #(r/as-element [faded-text %]) args)
        space-component (r/as-element [space])
        insert-spaces   (fn [str-or-vec]
                          (if (and (string? str-or-vec)
                                   (str/includes? str-or-vec "$space"))
                            (into [:<>]
                                  (-> str-or-vec
                                      (str/split #"\$space")
                                      (interleave (repeat space-component))
                                      add-keys))
                            str-or-vec))]
    [:> Text {:fontSize   "85%"
              :fontWeight "bold"
              :userSelect "all"
              :wordBreak  "break-word"}
     (as-> template t
           (str/split t #"\$text")
           (interleave t (concat faded-texts [nil]))
           (map insert-spaces t)
           (add-keys t)
           (into [:<>] t))]))


;; Help content
;; The examples use a small template language to insert the text (opaque) in the examples.
;; $text -> placeholder that will be replaced with opaque text, there can be many. The args passed
;;          passed after the template will replace the $text placeholders in order
;; $space -> small utility to render a space symbol.
;; ==========================
(def syntax-groups
  [{:name  "Bidirectional Link, Block References, and Tags"
    :items [{:description "Bidirectional Links"
             :example     [example "[[$text]]" "Athens"]}
            {:description "Labeled Bidirectional Link"
             :example     [example "[$text][[$text]]" "AS" "Alice Smith"]}
            {:description "Block Reference"
             :example     [example "(($text))" "Block ID"]}
            {:description "Labeled Block Reference"
             :example     [example "[$text](($text))" "Block text" "Block ID"]}
            {:description "Tag"
             :example     [example "#$text" "Athens"]}
            {:description "Tagged Link"
             :example     [example "#[[$text]]" "Athens"]}]}
   {:name  "Embeds"
    :items [{:description "Block"
             :example     [example "{{[[embed]]:(($text))}}" "reference to a block on a page"]}
            {:description "Image by URL"
             :example     [example "![$text]($text)" "Athens Logo" "https://avatars.githubusercontent.com/u/8952138"]}
            {:description "Youtube  Video"
             :example     [example "{{[[youtube]]:$text]]}}" "https://youtube.com/..."]}
            {:description "Web Page"
             :example     [example "{{iframe:<iframe src=\"$text\"></iframe>}}" "https://github.com/athensresearch/"]}
            {:description "Checkbox"
             :example     [example "{{[[TODO]]}}$space$text" "Label"]}]}
   {:name  "Markdown Formatting"
    :items [{:description "Labeled Link"
             :example     [example "[$text]($text)" "Athens" "http://athensresearch.org/"]}
            {:description "Link"
             :example     [example "<$text>" "http://athensresearch.org/"]}
            {:description "LaTeX"
             :example     [example "$$$text$$" "Your equation or mathematical symbol"]}
            {:description "Inline code"
             :example     [example "`$text`" "Inline Code"]}
            {:description "Highlight"
             :example     [example "^^$text^^" "Athens"]}
            {:description "Italicize"
             :example     [example "__$text__" "Athens"]}
            {:description "Bold"
             :example     [example "**$text**" "Athens"]}
            {:description "Underline"
             :example     [example "--$text--" "Athens"]}
            {:description "Strikethrough"
             :example     [example "~~$text~~" "Athens"]}
            {:description "Heading level 1"
             :example     [example "#$space$text" "Athens"]}
            {:description "Heading level 2"
             :example     [example "##$space$text" "Athens"]}
            {:description "Heading level 3"
             :example     [example "###$space$text" "Athens"]}
            {:description "Heading level 4"
             :example     [example "####$space$text" "Athens"]}
            {:description "Heading level 5"
             :example     [example "#####$space$text" "Athens"]}
            {:description "Heading level 6"
             :example     [example "######$space$text" "Athens"]}]}])


(def shortcut-groups
  [{:name  "App"
    :items [{:description "Toggle Search"
             :shortcut    "mod+k"}
            {:description "Toggle Left Sidebar"
             :shortcut    "mod+\\"}
            {:description "Toggle Right Sidebar"
             :shortcut    "mod+shift+\\"}
            {:description "Increase Text Size"
             :shortcut    "mod+plus"}
            {:description "Decrease Text Size"
             :shortcut    "mod+minus"}
            {:description "Reset Text Size"
             :shortcut    "mod+0"}]}
   {:name  "Input"
    :items [{:description "Autocomplete Menu"
             :shortcut    "/"}
            {:description "Indent selected block"
             :shortcut    "tab"}
            {:description "Unindent selected block"
             :shortcut    "shift+tab"}
            {:description "Undo"
             :shortcut    "mod+z"}
            {:description "Redo"
             :shortcut    "mod+shift+z"}
            {:description "Copy"
             :shortcut    "mod+c"}
            {:description "Paste"
             :shortcut    "mod+v"}
            {:description "Paste without formatting"
             :shortcut    "mod+shift+v"}
            {:description "Convert to checkbox"
             :shortcut    "mod+enter"}]}
   {:name  "Selection"
    :items [{:description "Select previous block"
             :shortcut    "shift+up"}
            {:description "Select next block"
             :shortcut    "shift+down"}
            {:description "Select all blocks"
             :shortcut    "mod+a"}]}
   {:name  "Formatting"
    :items [{:description "Bold"
             :example     [:strong "Athens"]
             :shortcut    "mod+b"}
            {:description "Italics"
             :example     [:i "Athens"]
             :shortcut    "mod+i"}
            ;; Underline is currently not working. Uncomment when it does.
            ;; {:description "Underline"
            ;; :example     [:span (use-style {:text-decoration "underline"}) "Athens"]
            ;; :shortcut    "mod+u"}
            {:description "Strikethrough"
             :example     [:> Text {:as "span" :textDecoration "line-through"} "Athens"]
             :shortcut    "mod+y"}
            {:description "Highlight"
             :example     [:> Text {:as "span"
                                    :background "highlight"
                                    :color "highlightContrast"
                                    :borderRadius "0.1rem"
                                    :padding "0 0.125em"}
                           "Athens"]
             :shortcut    "mod+h"}]}
   {:name  "Graph"
    :items [{:description "Open Node in Sidebar"
             :shortcut    "shift+click"}
            {:description "Move Node"
             :shortcut    "click+drag"}
            {:description "Zoom in/out"
             :shortcut    "scroll up/down"}]}])


(def content
  [{:name   "Syntax",
    :groups syntax-groups}
   {:name   "Keyboard Shortcuts"
    :groups shortcut-groups}])


;; Components to render content
;; =============================
(def mod-key
  (if (util/is-mac?) "⌘" "CTRL"))


(def alt-key
  (if (util/is-mac?) "⌥" "Alt"))


(defn shortcut
  [shortcut-str]
  (let [key-to-display (fn [key]
                         (-> key
                             (str/replace #"mod" mod-key)
                             (str/replace #"alt" alt-key)
                             (str/replace #"shift" "⇧")
                             (str/replace #"minus" "-")
                             (str/replace #"plus" "+")))
        keys           (as-> shortcut-str s
                             (str/split s #"\+")
                             (map key-to-display s))]
    [:> Box {:display "flex"
             :alignItems "center"
             :gap "0.3rem"}

     (doall
       (for [key keys]
         ^{:key key}
         [:> Text {:fontFamily "inherit"
                   :display "inline-flex"
                   :gap "0.3em"
                   :textTransform "uppercase"
                   :fontSize "0.8em"
                   :paddingInline "0.35em"
                   :background "background.basement"
                   :borderRadius "0.25rem"
                   :fontWeight 600}
          key]))]))


(defn help-section
  [title & children]
  [:> Box {:as "section"}
   [:> Heading {:as "h2"
                :color "foreground.primary"
                :textTransform "uppercase"
                :letterSpacing "0.06rem"
                :margin 0
                :font-weight 600
                :font-size "100%"
                :padding "1rem 1.5rem"}
    title]
   (doall
     (for [child children]
       ^{:key (hash child)}
       child))])


(defn help-section-group
  [title & children]
  [:> Box {:display "grid"
           :padding "1.5rem"
           :gridTemplateColumns "12rem 1fr"
           :columnGap "1rem"
           :borderTop "1px solid"
           :borderColor "separator.divider"}
   [:> Heading {:fontSize "1.5em"
                :as "h3"
                :margin 0
                :font-weight "bold"}
    title]
   [:div
    (doall
      (for [child children]
        ^{:key (hash child)}
        child))]])


(defn help-item
  [item]
  [:> Box {:borderRadius "0.5rem"
           :alignItems "center"
           :display "grid"
           :gap "1rem"
           :gridTemplateColumns "12rem 1fr"
           :padding "0.25rem 0.5rem"
           :sx {"&:nth-child(odd)"
                {:bg "background.floor"}}}
   [:> Text {:display "flex"
             :justify-content "space-between"}
    ;; Position of the example changes if there is a shortcut or not.
    (:description item)
    (when (contains? item :shortcut)
      (:example item))]
   (when (not (contains? item :shortcut))
     (:example item))
   (when (contains? item :shortcut)
     [shortcut (:shortcut item)])])


(defn help-popup
  []
  (r/with-let [open? (subscribe [:help/open?])
               close #(dispatch [:help/toggle])]
              [:> Modal {:isOpen             @open?
                         :onClose          close
                         :scrollBehavior "outside"
                         :size "full"}
               [:> ModalOverlay]
               [:> ModalContent {:maxWidth "calc(100% - 8rem)"
                                 :width "max-content"
                                 :my "4rem"}
                [:> ModalHeader "Help"
                 [:> ModalCloseButton]]
                [:> ModalBody {:flexDirection "column"}
                 (doall
                   (for [section content]
                     ^{:key section}
                     [help-section (:name section)
                      (doall
                        (for [group (:groups section)]
                          ^{:key group}
                          [help-section-group (:name group)
                           (doall
                             (for [item (:items group)]
                               ^{:key item}
                               [help-item item]))]))]))]]]))


