(ns athens.devcards.style-guide
  (:require
   [athens.style :as s]
   [cljsjs.react]
   [cljsjs.react.dom]
   [devcards.core :refer-macros [defcard defcard-rg]]
   [garden.core :refer [css]]
   [garden.stylesheet :refer [at-import]]
   ;;material-ui-icons
   ))



(def log js/console.log)

(def +circle (s/with-style {:width 80
                            :height 80
                            :border-radius 40}))


(def +flex-center
  (s/with-style {:display "flex" :flex-direction "column" :justify-content "center" :align-items "center"}))



(defcard-rg Colors
            [:div {:style {:display "flex" :justify-content "space-between"}}
             [:div (+flex-center)
              [:div ((comp +circle s/+blue-bg))]
              [:span "Blue"]
              [:span (:blue s/COLORS)]]
             [:div (+flex-center)
              [:div ((comp +circle s/+orange-bg))]
              [:span "Orange"]
              [:span (:orange s/COLORS)]]
             [:div (+flex-center)
              [:div ((comp +circle s/+red-bg))]
              [:span "Red"]
              [:span (:red s/COLORS)]]
             [:div (+flex-center)
              [:div ((comp +circle s/+green-bg))]
              [:span "Green"]
              [:span (:green s/COLORS)]]])

(defn main-css
  []
  [:style (css
            [:* {:font-family "Serif"}]
            [:h1 {:font-size "50px"
                  :font-weight 600
                  :line-height "65px"}]
            [:h2 {:font-size "38px"
                  :font-weight 500
                  :line-height "49px"}]
            [:h3 {:font-size "28px"
                  :font-weight 500
                  :line-height "36px"}]
            [:h4 {:font-size "21px"
                  :line-height "27px"}]
            [:h5 {:font-size "12px"
                  :font-weight 500
                  :line-height "16px"
                  :text-transform "uppercase"}])])

(def +flex-space-between
  (s/with-style {:display "flex" :align-items "center" :justify-content "space-between"}))

(def types [:h1 :h2 :h3 :h4 :h5])

(defcard-rg Serif-Types
            [:div
             [main-css]
             (for [t types]
               ^{:key t}
               [:div (+flex-space-between)
                [:span t]
                [t "Welcome to Athens"]])
             ])

(defcard Font "Not sure how to import fonts, e.g.
```
https://fonts.googleapis.com/css2?family=IBM+Plex+Sans:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;1,100;1,200;1,300;1,400;1,500;1,600;1,700
```
resources
- https://cljdoc.org/d/garden/garden/1.3.10/api/garden.stylesheet
- https://gist.github.com/paulkoegel/1c17be411c26d959fc6d75776d86e4f8
")


(defcard-rg Material-UI-Icons
            "Not sure how to import icons. Get the error:
            ```
            [:devcards] Build failure:\nThe required JS dependency \"material-ui/SvgIcon\" is not available, it was required by \"node_modules/material-ui-icons/AccessAlarm.js\".\n\nSearch in:\n        /Users/jefftang/code/athens/athens/node_modules\nYou probably need to run:\n  npm install material-ui/SvgIcon\n

            ```

            But I get the follow error when I try to install
            ```
            yarn add material-ui/SvgIcon\nyarn add v1.22.4\n[1/4] \uD83D\uDD0D  Resolving packages...\nerror Command failed.\nExit code: 128\nCommand: git\nArguments: ls-remote --tags --heads ssh://git@github.com/material-ui/SvgIcon.git\nDirectory: /Users/jefftang/code/athens/athens\nOutput:\nERROR: Repository not found.\nfatal: Could not read from remote repository.\n
            ```

            I tried creating a shim file like the rest of the `cljsjs` files.

            resources
            - https://shadow-cljs.github.io/docs/UsersGuide.html#cljsjs
            - https://github.com/cljsjs/packages/tree/master/material-ui-icons


            ")