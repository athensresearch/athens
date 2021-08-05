(ns athens.views.help
  (:require
    [clojure.string :as str]
    ["@material-ui/core/Modal" :default Modal]
    ["@material-ui/icons/Error" :default Error]
    ["@material-ui/icons/AddToPhotos" :default AddToPhotos]
    ["@material-ui/icons/LiveHelp" :default LiveHelp]
    [reagent.core :as r]
    [athens.util :as util]
    [athens.style :refer [color]]
    [stylefy.core :as stylefy :refer [use-style]]
    [re-frame.core :refer [dispatch subscribe]]))

;; Helpers to create the help content
;; ==========================
(defn opaque-text
  [text]
  [:span (use-style {:color (color :body-text-color :opacity-med) :font-weight "normal"}) text])

(defn space
  []
  [:i (use-style {:width         "0.25em"
                  :display       "inline-block"
                  :margin-inline "0.25em"
                  :height        "0.125em"
                  :border        (str "1px solid " (color :body-text-color :opacity-low))
                  :border-top    0})])

(defn example
  [template & args]
  (let [opaque-texts (map #(conj [opaque-text] %) args)
        insert-spaces (fn [str-or-vec]
                        (if (and (string? str-or-vec)
                                 (str/includes? str-or-vec "$space"))
                          (-> str-or-vec
                              (str/split #"\$space")
                              (interleave (repeat [space])))
                          str-or-vec))]
    [:span (use-style
             {:font-size   "85%"
              :font-weight "bold"
              :user-select "all"
              :word-break  "break-word"})
     (as-> template t
           (str/split t #"\$text")
           (interleave t (concat opaque-texts [nil]))
           (map insert-spaces t))]))

;; Help content
;; The examples use a small template language to insert the text (opaque) in the examples.
;; $text -> placeholder that will be replaced with opaque text, there can be many. The args passed
;;          passed after the template will replace the $text placeholders in order
;; $space -> small utility to render a space symbol.
;; ==========================
(def syntax-groups
  [{:name  "Links"
    :items [{:description "Link (or Create New Page)"
             :example     [example "[[$text]]" "Athens"]}
            {:description "Links to Block"
             :example     [example "(($text))" "Block ID"]}
            {:description "Labeled Link"
             :example     [example "[$text]($text)" "Athens" "http://athensresearch.org/"]}
            {:description "Link"
             :example     [example "<$text>" "http://athensresearch.org/"]}
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
             :example     [example "{{iframe:<iframe src=\"$text\"></iframe>}}" "https://github.com/athensresearch/"]}]}
   {:name  "Formatting"
    :items [{:description "LaTeX"
             :example     [example "$$$text$$" "Your equation or mathematical symbol"]}
            {:description "Checkbox"
             :example     [example "{{[[TODO]]}}$space$text" "Label"]}
            {:description "Inline code"
             :example     [example "`$text`" "Code"]}
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
    :items [{:description "Toggle Athena"
             :shortcut    "mod+k"}
            {:description "Toggle Navigation"
             :shortcut    "mod+s"}
            {:description "Toggle Reference Sidebar"
             :shortcut    "mod+shift+s"}
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
             :shortcut    "shift+a"}]}
   {:name  "Formatting"
    :items [{:description "Bold"
             :example     [:strong "Athens"]
             :shortcut    "mod+b"}
            {:description "Italics"
             :example     [:i "Athens"]
             :shortcut    "mod+i"}
            {:description "Underline"
             :example     [:span (use-style {:text-decoration "underline"}) "Athens"]
             :shortcut    "mod+u"}
            {:description "Strikethrough"
             :example     [:span (use-style {:text-decoration "line-through"}) "Athens"]
             :shortcut    "mod+shift+x"}
            {:description "Highlight"
             :example     [:span (use-style {:background    (color :highlight-color)
                                             :color         (color :background-color)
                                             :border-radius "0.1rem"
                                             :padding       "0 0.125em"}) "Athens"]
             :shortcut    "mod+h"}]}
   {:name  "Graph"
    :items [{:description "Open Node in Sidebar"
             :shortcut    "shift+click"}
            {:description "Zoom to Node"
             :shortcut    "shift+click+hold"}
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
  [:div (use-style {:display     "flex"
                    :align-items "center"
                    :gap         "0.3rem"})
   (let [key-to-display (fn [key]
                          (-> key
                              (str/replace #"mod" mod-key)
                              (str/replace #"alt" alt-key)
                              (str/replace #"shift" "⇧")
                              (str/replace #"minus" "-")
                              (str/replace #"plus" "+")))
         keys (as-> shortcut-str s
                    (str/split s #"\+")
                    (map key-to-display s))]


     (for [key keys]
       ^{:key key} [:span (use-style {:font-family    "inherit"
                                      :display        "inline-flex"
                                      :gap            "0.3em"
                                      :text-transform "uppercase"
                                      :font-size      "0.8em"
                                      :padding-inline "0.35em"
                                      :background     (color :background-plus-2)
                                      :border-radius  "0.25rem"
                                      :font-weight    600})
                    key]))])

(def modal-body-styles
  {:width         "max-content"
   :margin        "2rem auto"
   :background    (color :background-plus-1)
   :border        (str "1px solid " (color :border-color))
   :max-width     "calc(100% - 1rem)"
   :border-radius "1rem"
   :box-shadow    (str "0 0.25rem 0.5rem -0.25rem " (color :shadow-color))
   :display       "flex"})

(def help-styles
  {:background-color (color :background-color)
   :border-radius    "1rem"
   :display          "flex"
   :flex-direction   "column"
   :min-width        "500px"})

(def help-header-styles
  {:display         "flex"
   :justify-content "space-between"
   :margin          0
   :align-items     "center"
   :border-bottom   [["1px solid" (color :border-color)]]})

(def help-title
  {:padding   "1rem 1.5rem"
   :margin    "0"
   :font-size "2rem"
   :color     (color :header-text-color)})

(defn help-section
  [title & children]
  [:section
   [:h2 (use-style
          {:color          (color :body-text-color :opacity-med)
           :text-transform "uppercase"
           :letter-spacing "0.06rem"
           :margin         0
           :font-weight    600
           :font-size      "100%"
           :padding        "1rem 1.5rem"})
    title]
   children])

(defn help-section-group
  [title & children]
  [:section (use-style
              {:display               "grid"
               :padding               "1.5rem"
               :grid-template-columns "12rem 1fr"
               :column-gap            "1rem"
               :border-top            [["1px solid" (color :border-color)]]})
   [:h3 (use-style {:font-size   "1.5em"
                    :margin      0
                    :font-weight "bold"})
    title]
   [:div children]])

(defn help-item
  [item]
  [:div (use-style
          {:border-radius         "0.5rem"
           :align-items           "center"
           :display               "grid"
           :gap                   "1rem"
           :grid-template-columns "12rem 1fr"
           :padding               "0.25rem 0.5rem"
           ::stylefy/manual       [["&:nth-child(odd)"
                                    {:background (color :background-plus-2 :opacity-low)}]]})

   [:span (use-style
            {:display         "flex"
             :justify-content "space-between"})
    ;; Position of the example changes if there is a shortcut or not.
    (:description item)
    (when (contains? item :shortcut)
      (:example item))]
   (when (not (contains? item :shortcut))
     (:example item))
   (when (contains? item :shortcut)
     [shortcut (:shortcut item)])])

(defn modal-body
  [& children]
  [:div (use-style modal-body-styles) children])

(defn help-link
  [& children]
  [:a (use-style
        {:color           (color :body-text-color)
         :padding         "0.25rem 0.5rem"
         :text-decoration "underline"
         :font-size       "80%"
         :display         "flex"
         :align-items     "center"
         :gap             "0.25rem"
         ::stylefy/manual [["svg" {:font-size "1.5em"}]]}
        {:target "_blank" :rel "noopener noreferrer"})
   children])

(defn help-popup
  []
  (let [open? @(subscribe [:help/open?])]
    (if open?
      [:> Modal {:open    true
                 :style   {:overflow-y "auto"}
                 :disableEnforceFocus true
                 :disableAutoFocus true
                 :onClose #(dispatch [:help/toggle])}
       [modal-body
        [:div (use-style help-styles)
         [:header (use-style help-header-styles)
          [:h1 (use-style help-title)
           "Help"]
          [:nav
           (use-style
             {:display "flex"
              :gap     "1rem"
              :padding "1rem"})]]
         ;; Links at the top of the help. Uncomment when the correct links are obtained.
         ;;[help-link
         ;; [:> LiveHelp]
         ;; "Get Help on Discord"]
         ;;[help-link
         ;; [:> Error]
         ;; "Get Help on Discord"]
         ;;[help-link
         ;; [:> AddToPhotos]
         ;; "Get Help on Discord"]]]
         [:div (use-style {:overflow-y "auto"})
          (for [section content]
            ^{:key section}
            [help-section (:name section)
             (for [group (:groups section)]
               ^{:key group}
               [help-section-group (:name group)
                (for [item (:items group)]
                  ^{:key item}
                  [help-item item])])])]]]])))


