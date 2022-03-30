(ns athens.views.pages.header
  (:require
    ["@chakra-ui/react" :refer [Box]]))


(def title-style-props
  {:position "relative"
   :fontSize "2rem"
   :overflow "visible"
   :flexGrow "1"
   :margin "0"
   :whiteSpace "pre-line"
   :wordBreak "break-word"
   :fontWeight "bold"})


(defn page-header
  ([_ children]
   [:> Box {:as "header"
            :class "page-header"
            :position "relative"}
    children]))


(defn title-container
  [children]
  [:> Box title-style-props
   children])


(defn editable-title-container
  ([props children]
   (let [{:keys [_]} props]
     [:> Box (merge
               title-style-props
               {:as "header"
                :class "page-header"
                :sx {"textarea" {:appearance    "none"
                                 :cursor        "text"
                                 :resize        "none"
                                 :transform     "translate3d(0,0,0)"
                                 :color         "inherit"
                                 :fontWeight    "inherit"
                                 :padding       "0"
                                 :letterSpacing "inherit"
                                 :width         "100%"
                                 :minHeight     "100%"
                                 :caretColor    "link"
                                 :background    "transparent"
                                 :margin        "0"
                                 :fontSize      "inherit"
                                 :lineHeight    "inherit"
                                 :borderRadius  "0.25rem"
                                 :transition    "opacity 0.15s ease"
                                 :border        "0"
                                 :fontFamily    "inherit"
                                 :visibility    "hidden"
                                 :position      "absolute"}
                     {"textarea ::WebkitScrollbar" {:display "none"}}
                     {".is-editing textarea:focus" {:outline "none"
                                                    :visibility "visible"
                                                    :position "relative"}}
                     "abbr" {:z-index 4}
                     ".is-editing span" {:visibility "hidden"
                                         :position   "absolute"}}})
      children])))
