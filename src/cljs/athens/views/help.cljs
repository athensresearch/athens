(ns athens.views.help
  (:require
    ["@material-ui/core/Modal" :default Modal]
    ["@material-ui/icons/Error" :default Error]
    ["@material-ui/icons/AddToPhotos" :default AddToPhotos]
    ["@material-ui/icons/LiveHelp" :default LiveHelp]
    [reagent.core :as r]
    [athens.style :refer [color]]
    [stylefy.core :as stylefy :refer [use-style]]
    [re-frame.core :refer [dispatch subscribe]]))

(def modal-body-styles
  {:width "max-content"
   :margin "auto"
   :margin-top "20vh"})

(def help-styles
  {:background-color (color :background-color)
   :border-radius "1.5rem"
   :min-width "500px"})

(def help-header-styles
  {:display "flex"
   :justify-content "space-between"
   :align-items "center"
   :border-bottom [["1px solid" (color :border-color)]]})

(def help-title
  {:padding "1rem 1.5rem"
   :font-size "2rem"
   :color (color :header-text-color)})

(defn help-section
  [title & children]
  [:section (use-style
              {:display "grid"
               :grid-template-columns "12rem 1fr"
               :border-bottom [["1px solid" (color :border-color)]]})
   [:h2 (use-style {:font-size "1.5em"
                    :padding "1.5rem 1.5rem 0"
                    :font-weight "bold"})
    title]
   [:div (use-style
           {:display "gird"
            :grid-template-columns "12rem 1fr"
            :column-gap "1rem"})
    children]])

(defn help-item
  [& children]
  [:div children])

(defn modal-body
  [& children]
  [:div (use-style modal-body-styles) children])

(defn help-link
  [& children]
  [:a (use-style
        {:color (color :body-text-color)
         :padding "0.25rem 0.5rem"
         :text-decoration "underline"
         :font-size "80%"
         :display "flex"
         :align-items "center"
         :gap "0.25rem"
         ::stylefy/manual [["svg" {:font-size "1.5em"}]]}
        {:target "_blank" :rel "noopener noreferrer"})
   children])

(defn help-popup
  []
  (let [open? @(subscribe [:help/open?])]
    (if open?
      [:> Modal {:open    true
                 :onClose #(js/console.log "CLOSE")}
       [modal-body
        [:div (use-style help-styles)
         [:header (use-style help-header-styles)
          [:h1 (use-style help-title)
           "Help"]
          [:nav
           (use-style
             {:display "flex"
              :gap "1rem"
              :padding "1rem"})
           [help-link
            [:> LiveHelp]
            "Get Help on Discord"]
           [help-link
            [:> Error]
            "Get Help on Discord"]
           [help-link
            [:> AddToPhotos]
            "Get Help on Discord"]]]
         [help-section "Links"
          [help-item
           [:div "Link to Block"]]]]]])))


