(ns athens.views.app-toolbar
  (:require
    ["/components/AppToolbar/AppToolbar" :refer [AppToolbar]]
    [athens.electron.db-menu.core :refer [db-menu]]
    [athens.electron.db-modal :as db-modal]
    [athens.electron.utils :as electron.utils]
    [athens.router :as router]
    [athens.self-hosted.presence.views :refer [toolbar-presence-el]]
    [athens.subs]
    [athens.util :as util]
    [re-frame.core :refer [subscribe dispatch]]
    [reagent.core :as r]))


(defn app-toolbar
  []
  (let [left-open?        (subscribe [:left-sidebar/open])
        right-open?       (subscribe [:right-sidebar/open])
        route-name        (subscribe [:current-route/name])
        os                (util/get-os)
        electron?         (util/electron?)
        theme-dark        (subscribe [:theme/dark])
        win-focused?      (if electron?
                            (subscribe [:win-focused?])
                            (r/atom false))
        win-maximized?    (if electron?
                            (subscribe [:win-maximized?])
                            (r/atom false))
        win-fullscreen?   (if electron?
                            (subscribe [:win-fullscreen?])
                            (r/atom false))
        merge-open?       (reagent.core/atom false)

        selected-db       (subscribe [:db-picker/selected-db])]
    (fn []
      [:<>
       (when @merge-open?
         [db-modal/merge-modal merge-open?])
       [:> AppToolbar {:os os
                       :isElectron electron?
                       :route @route-name
                       :isWinFullscreen @win-fullscreen?
                       :isWinMaximized @win-maximized?
                       :isWinFocused @win-focused?
                       :isThemeDark @theme-dark
                       :isLeftSidebarOpen @left-open?
                       :isRightSidebarOpen @right-open?
                       :isCommandBarOpen @(subscribe [:athena/open])
                       :onPressLeftSidebarToggle #(dispatch [:left-sidebar/toggle])
                       :onPressHistoryBack #(.back js/window.history)
                       :onPressHistoryForward #(.forward js/window.history)
                       :onPressDailyNotes router/nav-daily-notes
                       :onPressAllPages #(router/navigate :pages)
                       :onPressGraph #(router/navigate :graph)
                       :onPressCommandBar #(dispatch [:athena/toggle])
                       :onPressThemeToggle #(dispatch [:theme/toggle])
                       :onPressSettings #(router/navigate :settings)
                       :onPressMerge #(swap! merge-open? not)
                       :onPressRightSidebarToggle #(dispatch [:right-sidebar/toggle])
                       :databaseMenu (r/as-element [db-menu])
                       :presenceDetails (when (electron.utils/remote-db? @selected-db)
                                          (r/as-element [toolbar-presence-el]))}]])))
