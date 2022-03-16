(ns athens.views.app-toolbar
  (:require
    ["/components/AppToolbar/AppToolbar" :refer [AppToolbar]]
    [athens.electron.db-menu.core        :refer [db-menu]]
    [athens.electron.db-modal            :as db-modal]
    [athens.electron.utils               :as electron.utils]
    [athens.router                       :as router]
    [athens.self-hosted.presence.views   :refer [toolbar-presence-el]]
    [athens.style                        :refer [unzoom]]
    [athens.subs]
    [athens.util                         :as util]
    [re-frame.core                       :as rf]
    [reagent.core                        :as r]))


(defn app-toolbar
  []
  (let [left-open?             (rf/subscribe [:left-sidebar/open])
        right-open?            (rf/subscribe [:right-sidebar/open])
        help-open?             (rf/subscribe [:help/open?])
        athena-open?           (rf/subscribe [:athena/open])
        route-name             (rf/subscribe [:current-route/name])
        theme-dark             (rf/subscribe [:theme/dark])
        selected-db            (rf/subscribe [:db-picker/selected-db])
        electron?              electron.utils/electron?
        win-focused?           (if electron?
                                 (rf/subscribe [:win-focused?])
                                 (r/atom false))
        win-maximized?         (if electron?
                                 (rf/subscribe [:win-maximized?])
                                 (r/atom false))
        win-fullscreen?        (if electron?
                                 (rf/subscribe [:win-fullscreen?])
                                 (r/atom false))
        merge-open?            (r/atom false)
        os                     (util/get-os)
        on-left-sidebar-toggle #(rf/dispatch [:left-sidebar/toggle])
        on-back                #(.back js/window.history)
        on-forward             #(.forward js/window.history)
        on-daily-pages         (fn [_]
                                 (rf/dispatch [:reporting/navigation {:source :app-toolbar
                                                                      :target :home
                                                                      :pane   :main-pane}])
                                 (router/nav-daily-notes))
        on-all-pages           (fn [_]
                                 (rf/dispatch [:reporting/navigation {:source :app-toolbar
                                                                      :target :all-pages
                                                                      :pane   :main-pane}])
                                 (router/navigate :pages))
        on-graph               (fn [_]
                                 (rf/dispatch [:reporting/navigation {:source :app-toolbar
                                                                      :target :graph
                                                                      :pane   :main-pane}])
                                 (router/navigate :graph))
        on-settings            (fn [_]
                                 (rf/dispatch [:reporting/navigation {:source :app-toolbar
                                                                      :target :settings
                                                                      :pane   :main-pane}])
                                 (router/navigate :settings))
        on-athena              #(rf/dispatch [:athena/toggle])
        on-help                #(rf/dispatch [:help/toggle])
        on-theme               #(rf/dispatch [:theme/toggle])
        on-merge               #(swap! merge-open? not)
        on-right-sidebar       #(rf/dispatch [:right-sidebar/toggle])
        on-maximize            #(rf/dispatch [:toggle-max-min-win])
        on-minimize            #(rf/dispatch [:minimize-win])
        on-close               #(rf/dispatch [:close-win])]
    (fn []
      [:<>
       (when @merge-open?
         [db-modal/merge-modal merge-open?])
       [:> AppToolbar {:style                     (unzoom)
                       :os                        os
                       :isElectron                electron?
                       :route                     @route-name
                       :isWinFullscreen           @win-fullscreen?
                       :isWinMaximized            @win-maximized?
                       :isWinFocused              @win-focused?
                       :isHelpOpen                @help-open?
                       :isThemeDark               @theme-dark
                       :isLeftSidebarOpen         @left-open?
                       :isRightSidebarOpen        @right-open?
                       :isCommandBarOpen          @athena-open?
                       :onPressLeftSidebarToggle  on-left-sidebar-toggle
                       :onPressHistoryBack        on-back
                       :onPressHistoryForward     on-forward
                       :onPressDailyNotes         on-daily-pages
                       :onPressAllPages           on-all-pages
                       :onPressGraph              on-graph
                       :onPressCommandBar         on-athena
                       :onPressHelp               on-help
                       :onPressThemeToggle        on-theme
                       :onPressSettings           on-settings
                       :onPressMerge              on-merge
                       :onPressRightSidebarToggle on-right-sidebar
                       :onPressMaximizeRestore    on-maximize
                       :onPressMinimize           on-minimize
                       :onPressClose              on-close
                       :databaseMenu              (r/as-element [db-menu])
                       :presenceDetails           (when (electron.utils/remote-db? @selected-db)
                                                    (r/as-element [toolbar-presence-el]))}]])))
