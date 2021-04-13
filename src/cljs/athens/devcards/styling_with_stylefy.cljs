(ns athens.devcards.styling-with-stylefy
  (:require
    [athens.db]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard-doc]]))


(defcard-doc
  "# Styling in Athens

  Components in Athens are styled using [Stylefy](https://github.com/Jarzka/stylefy).
  
  Behind the scenes, Stylefy creates classes and links them to each rendered component. This avoids polluting browser markup with inline styles, provides clear abstractions for reasoning about components, and allows for style reuse.
")


(defcard-doc "
  ## Creating Styles

  It's helpful to distinguish style definitions from other types of definitions. Append `-style` to the name of your component when defining the style: `(def blocks-style)`, `(page-style)`.

  Stylefy Styles are written as Clojure maps, so they should look familiar

  ```clojure
  (def button-style
    {:cursor \"pointer\"
     :padding \"0.5rem 0.75rem\"})
  ```
")


(defcard-doc "
  Style pseudo-elements and element modes with `::stylefy/mode` inside the style definition.

  ```clojure
  (def button-style
    {:cursor \"pointer\"
      :padding \"0.5rem 0.75rem\"
      ::stylefy/mode [[:hover {:background-color \"blue\"}]
                      [:after {:content \"''\"}]]})
  ```
")


(defcard-doc "
  Create styles for inner components with Stylefy's \"sub-styles\". Use these when creating sub-components that won't be used separately.

  ```clojure
  (def left-sidebar-style
    {:display \"flex\"
     ::stylefy/sub-styles {:footer {:margin-top \"auto\"}}})
  ```
")


(defcard-doc "
  Create deeper selections with `::stylefy/manual`.

  ```clojure
  (def block-indicator-style
    {:width \"0.75em\"
     :height \"2em\"
     ::stylefy/manual [[:&.open {:color \"blue\"}]})
  ```
")


(defcard-doc "
  Create more complex selections where necessary by combining manual mode with [Garden](https://github.com/noprompt/garden)'s advanced selectors.

  ```clojure
  (:require [garden.selectors :as selector])

  (def block-indicator-style
    {:width \"0.75em\"
     :height \"2em\"
     ::stylefy/mode [[:before {:content \"'hello'\"}]]
     ::stylefy/manual [[:&.closed [(selectors/& (selectors/before)) {:content \"none\"}]]})
  ```

  Check out Stylefy's [documentation](https://github.com/Jarzka/stylefy) to learn about composing styles together, creating dynamic style functions, and more.
")


(defcard-doc "
  ### Applying Styles

  Connect your new style with the component with Stylefy's `(use-style)` function. `(use-style)` accepts two arguments: the style to add, and attributes to apply to the component.

  ```clojure
  (:require [stylefy.core :as stylefy :refer [use-style]])

  (def box-style
    {:border \"1px solid\"})

  (defn box-component
    [:div (use-style box-style)])
  ```")


(defcard-doc "
  Provide attributes to the element by adding them to `(use-style)` after the style.

  ```clojure
  [:a (use-style link-style {:href \"https://athensresearch.github.io/athens\"} \"Athens\")]
  ```
")


(defcard-doc "
  Apply sub-styles with Stylefy's `(use-sub-style)` function.
              
  Don't create empty sub-styles, as this may silently break Stylefy processing.

  ```clojure
  (:require [stylefy.core :as stylefy :refer [use-style]])

  (def box-style {:border \"1px solid\"
                  ::stylefy/sub-styles {:box-child {:background \"blue\"}})

  (defn box-component
    [:div (use-style box-style)
      [:div (use-sub-style box-style :box-child)]])
  ```
  ")


(defcard-doc "
  Avoid creating styles that will be frequently updated, because this forces Stylefy to create a new class for each update.
  
  In these cases, merge `use-style` with a style attribute to perform the updates inline, bypassing `use-style`.
  
  ```clojure
  [:div (merge (use-style cursor-trail-style) {:style {:left x :top y}})]
  ```")
