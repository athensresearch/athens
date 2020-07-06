(ns athens.devcards.dropdown
  (:require
    ["@material-ui/icons" :as mui-icons]
    [athens.views.dropdown :refer [dropdown menu-item menu kbd submenu-indicator menu-separator menu-heading]]
    [athens.views.filters :refer [filters-el]]
    [athens.views.textinput :refer [textinput]]
    [devcards.core :refer-macros [defcard-rg]]))


(defcard-rg Slash-Menu
  [dropdown {:content
             [:<>
              [textinput {:placeholder "Type to filter commands"}]
              [menu {:style {:max-height "8em"} :content
                     [:<>
                      [menu-item {:label [:<> [:> mui-icons/Done] [:span "Add Todo"] [kbd "cmd-enter"]]}]
                      [menu-item {:label [:<> [:> mui-icons/Description] [:span "Page Reference"] [kbd "[["]]}]
                      [menu-item {:label [:<> [:> mui-icons/Link] [:span "Block Reference"] [kbd "(("]]}]
                      [menu-item {:label [:<> [:> mui-icons/Timer] [:span "Current Time"]]}]
                      [menu-item {:label [:<> [:> mui-icons/DateRange] [:span "Date Picker"]]}]
                      [menu-item {:label [:<> [:> mui-icons/Attachment] [:span "Upload Image or File"]]}]
                      [menu-item {:label [:<> [:> mui-icons/ExposurePlus1] [:span "Word Count"]]}]
                      [menu-item {:label [:<> [:> mui-icons/Today] [:span "Today"]]}]]}]]}])


(defcard-rg Block-Dropdown-Menu
  [dropdown {:content
             [menu {:content
                    [:<>
                    ;;  [menu-heading "Modify Block 'Day of Datomic On-Prem 2016'"]
                    ;;  [textinput {:icon [:> mui-icons/Face] :placeholder "Type to filter"}]
                     [menu-item {:label [:<> [:> mui-icons/Link] [:span "Copy Page Reference"]]}]
                     [menu-item {:label [:<> [:> mui-icons/Star] [:span "Add to Shortcuts"]]}]
                     [menu-item {:label [:<> [:> mui-icons/Face] [:span "Add Reaction"] [submenu-indicator]]}]
                     [menu-separator]
                     [menu-item {:label [:<> [:> mui-icons/LastPage] [:span "Open in Sidebar"] [kbd "shift-click"]]}]
                     [menu-item {:label [:<> [:> mui-icons/Launch] [:span "Open in New Window"] [kbd "ctrl-o"]]}]
                     [menu-item {:label [:<> [:> mui-icons/UnfoldMore] [:span "Expand All"]]}]
                     [menu-item {:label [:<> [:> mui-icons/UnfoldLess] [:span "Collapse All"]]}]
                     [menu-item {:label [:<> [:> mui-icons/Slideshow] [:span "View As"]  [submenu-indicator]]}]
                     [menu-separator]
                     [menu-item {:label [:<> [:> mui-icons/FileCopy] [:span "Duplicate and Break Links"]]}]
                     [menu-item {:label [:<> [:> mui-icons/LibraryAdd] [:span "Save as Template"]]}]
                     [menu-item {:label [:<> [:> mui-icons/History] [:span "Browse Versions"]]}]
                     [menu-item {:label [:<> [:> mui-icons/CloudDownload] [:span "Export As"]]}]]}]}])


(def items
  {"Amet"   {:count 6 :state :added}
   "At"     {:count 130 :state :excluded}
   "Diam"   {:count 6}
   "Donec"  {:count 6}
   "Elit"   {:count 30}
   "Elitudomin mesucen defibocutruon"  {:count 1}
   "Erat"   {:count 11}
   "Est"    {:count 2}
   "Eu"     {:count 2}
   "Ipsum"  {:count 2 :state :excluded}
   "Magnis" {:count 10 :state :added}
   "Metus"  {:count 29}
   "Mi"     {:count 7 :state :added}
   "Quam"   {:count 1}
   "Turpis" {:count 97}
   "Vitae"  {:count 1}})


(defcard-rg With-Filters-and-custom-style-accommodations
  [dropdown {:style {:width "20em" :height "20em"}
             :content [:<>
                       [menu-heading "Filters"]
                       [filters-el "((some-uid))" items]]}])
