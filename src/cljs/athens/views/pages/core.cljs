(ns athens.views.pages.core
  (:require
    [athens.style :as style]
    [athens.views.pages.all-pages :as all-pages]
    [athens.views.pages.daily-notes :as daily-notes]
    [athens.views.pages.graph :as graph]
    [athens.views.pages.page :as page]
    [athens.views.pages.settings :as settings]
    [re-frame.core :as rf]
    [stylefy.core :as stylefy]))


;; Styles

(def main-content-style
  {:flex "1 1 100%"
   :grid-area "main-content"
   :align-items "flex-start"
   :justify-content "stretch"
   :padding-top "2.5rem"
   :display "flex"
   :overflow-y "auto"
   ::stylefy/mode {"::-webkit-scrollbar" {:background (style/color :background-minus-1)
                                          :width "0.5rem"
                                          :height "0.5rem"}
                   "::-webkit-scrollbar-corner" {:background (style/color :background-minus-1)}
                   "::-webkit-scrollbar-thumb" {:background (style/color :background-minus-2)
                                                :border-radius "0.5rem"}}})


;; Helpers


(defn match-page
  "When app initializes, `route-name` is `nil`. Side effect of this is that a daily page for today is automatically
  created when app inits. This is expected, but perhaps shouldn't be a side effect here."
  [route-name]
  [(case route-name
     :settings settings/page
     :pages all-pages/page
     :page page/page
     :graph graph/page
     :home daily-notes/page
     daily-notes/page)])


;; View

(defn view
  []
  (let [route-name (rf/subscribe [:current-route/name])]
    [:div (stylefy/use-style main-content-style
                             {:on-scroll (when (= @route-name :home)
                                           #(daily-notes/db-scroll-daily-notes %))})
     [match-page @route-name]]))
