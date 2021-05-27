(ns athens.theme)


(def THEME-DARK
  {:link-color                       "#2399E7"
   :highlight-color                  "#FBBE63"
   :text-highlight-color             "#FBBE63"
   :warning-color                    "#DE3C21"
   :confirmation-color               "#189E36"
   :header-text-color                "#BABABA"
   :body-text-color                  "#AAA"
   :border-color                     "hsla(32, 81%, 90%, 0.08)"
   :background-minus-1               "#151515"
   :background-minus-2               "#111"
   :background-color                 "#1A1A1A"
   :background-plus-1                "#222"
   :background-plus-2                "#333"

   :graph-control-bg                 "#272727"
   :graph-control-color              "white"
   :graph-node-normal                "#909090"
   :graph-node-hlt                   "#FBBE63"
   :graph-link-normal                "#323232"

   :error-color                      "#fd5243"

   ;; find in page styles
   :find-in-page-background          "#151515"
   :find-in-page-text-color          "white"
   :find-in-page-icon-hover-bg-color "rgb(67 63 56 / 65%)"
   :find-in-page-text-color-light    "#cecece"})


(def THEME-LIGHT
  {:link-color                       "#0075E1"
   :highlight-color                  "#F9A132"
   :text-highlight-color             "#ffdb8a"
   :warning-color                    "#D20000"
   :confirmation-color               "#009E23"
   :header-text-color                "#322F38"
   :body-text-color                  "#433F38"
   :border-color                     "hsla(32, 81%, 10%, 0.08)"
   :background-plus-2                "#fff"
   :background-plus-1                "#fbfbfb"
   :background-color                 "#F6F6F6"
   :background-minus-1               "#FAF8F6"
   :background-minus-2               "#EFEDEB"
   :graph-control-bg                 "#f9f9f9"
   :graph-control-color              "black"
   :graph-node-normal                "#909090"
   :graph-node-hlt                   "#0075E1"
   :graph-link-normal                "#cfcfcf"

   :error-color                      "#fd5243"

   ;; find in page styles
   :find-in-page-background          "rgb(245 245 245 / 75%)"
   :find-in-page-text-color          "black"
   :find-in-page-icon-hover-bg-color "rgb(67 63 56 / 10%)"
   :find-in-page-text-color-light    "#3e3e3e"})
