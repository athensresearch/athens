(ns athens.views.app-toolbar
  (:require
    ["/components/AppToolbar/AppToolbar" :refer [AppToolbar]]
    [athens.common-db                    :as common-db]
    [athens.db                           :as db]
    [athens.electron.db-menu.core        :refer [db-menu]]
    [athens.electron.utils               :as electron.utils]
    [athens.router                       :as router]
    [athens.self-hosted.presence.views   :refer [toolbar-presence-el]]
    [athens.style                        :refer [unzoom]]
    [athens.subs]
    [athens.util                         :as util]
    [athens.views.comments.core          :as comments]
    [athens.views.notifications.core     :as notifications]
    [athens.views.notifications.popover  :refer [notifications-popover]]
    [re-frame.core                       :as rf]
    [reagent.core                        :as r]))


(def name-from-route
  {:home "Daily Notes"
   :graph "Graph"})


(defn app-toolbar
  []
  (let [current-page-title     (rf/subscribe [:current-route/page-title])
        left-open?             (rf/subscribe [:left-sidebar/open])
        right-open?            (rf/subscribe [:right-sidebar/open])
        help-open?             (rf/subscribe [:help/open?])
        athena-open?           (rf/subscribe [:athena/open])
        show-comments?         (rf/subscribe [:comment/show-comments?])
        route-name             (rf/subscribe [:current-route/name])
        route-uid              (rf/subscribe [:current-route/uid])
        theme-dark             (rf/subscribe [:theme/dark])
        selected-db            (rf/subscribe [:db-picker/selected-db])
        notificationsPopoverOpen? (rf/subscribe [:notification/show-popover?])
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
        os                     (util/get-os)
        on-left-sidebar-toggle #(rf/dispatch [:left-sidebar/toggle])
        on-back                (fn [_]
                                 (rf/dispatch [:reporting/navigation {:source :app-toolbar
                                                                      :target :back
                                                                      :pane   :main-pane}])
                                 (.back js/window.history))
        on-forward             (fn [_]
                                 (rf/dispatch [:reporting/navigation {:source :app-toolbar
                                                                      :target :forward
                                                                      :pane   :main-pane}])
                                 (.forward js/window.history))
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
                                 (rf/dispatch [:settings/toggle-open]))
        on-athena              #(rf/dispatch [:athena/toggle])
        on-help                #(rf/dispatch [:help/toggle])
        on-theme               #(rf/dispatch [:theme/toggle])
        on-right-sidebar       #(rf/dispatch [:right-sidebar/toggle])
        on-maximize            #(rf/dispatch [:toggle-max-min-win])
        on-minimize            #(rf/dispatch [:minimize-win])
        on-close               #(rf/dispatch [:close-win])]

    [:> AppToolbar
     (merge
       {:style                     (unzoom)
        :os                        os
        :isElectron                electron?
        :currentLocationName       (or @current-page-title
                                       (common-db/get-block-string @db/dsdb @route-uid)
                                       (name-from-route @route-name))
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
        :onPressRightSidebarToggle on-right-sidebar
        :onPressMaximizeRestore    on-maximize
        :onPressMinimize           on-minimize
        :currentPageTitle          (or @current-page-title nil)
        :onPressClose              on-close
        :workspacesMenu              (r/as-element [db-menu])
        :presenceDetails           (when (electron.utils/remote-db? @selected-db)
                                     (r/as-element [toolbar-presence-el]))}
       (when (notifications/enabled?)
         {:notificationPopover (r/as-element [:f> notifications-popover])
          :isNotificationsPopoverOpen @notificationsPopoverOpen?})
       (when (comments/enabled?)
         {:isShowComments  @show-comments?
          :onClickComments #(rf/dispatch [:comment/toggle-comments])}))]))
