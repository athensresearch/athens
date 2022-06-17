(ns athens.athens-datoms
  (:require
    [athens.common-db :as common-db]
    [athens.common-events :as common-events]
    [athens.common-events.bfs :as bfs]
    [athens.common-events.graph.atomic :as atomic-graph-ops]))


(def welcome-page-title "Welcome")


(def welcome-page-internal-representation
  [{:page/title     welcome-page-title
    :block/children [#:block{:uid "ee770c334",
                             :string "Welcome to Athens, Open-Source Networked Thought!",}
                     #:block{:uid "6aecd4172",
                             :string "You can open and close blocks that have children.",
                             :children [#:block{:uid "5f82a48ef",
                                                :string "![](https://athens-assets-1.s3.us-east-2.amazonaws.com/welcome.gif)",}]}
                     #:block{:uid "7e409b1cb",
                             :string "**How to Use Athens**",
                             :open? false,
                             :children [#:block{:uid "289cc9981",
                                                :string "Outliner Features",
                                                :open? false,
                                                :children [#:block{:uid "62b9428d5",
                                                                   :string "You can click on a bullet • to zoom in on it.",
                                                                   :children [#:block{:uid "70907b596",
                                                                                      :string "You can navigate back to a higher context by clicking on navigation breadcrumbs (when zoomed in).",}]}
                                                           #:block{:uid "c312c0f9a",
                                                                   :string "Indent and unindent bullets with tab and shift-tab.",}
                                                           #:block{:uid "59ccb6c73",
                                                                   :string "Drag and drop bullets to re-order blocks.",}
                                                           #:block{:uid "a8589c828",
                                                                   :string "Select multiple bullets with click and drag or shift-up or shift-down.",
                                                                   :children [#:block{:uid "6cf8e69b2",
                                                                                      :string "You can drag and drop with multiple blocks selected!",}]}]}
                                        #:block{:uid "6b8c28b09",
                                                :string "Markup Features",
                                                :open? false,
                                                :children [#:block{:uid "e434db606",
                                                                   :string "To edit the raw-text of a block, simply click on it and begin typing!",}
                                                           #:block{:uid "e5dec8a28",
                                                                   :string "Bold text with **double asterisks**",}
                                                           #:block{:uid "3949afab9",
                                                                   :string "Mono-spaced text with `backticks`"}
                                                           #:block{:uid "a8760ca6d",
                                                                   :string "Links with `[[]]`, `#`, or `#[[]]`: [[Welcome]] #Welcome #[[Welcome]]",
                                                                   :open? false,
                                                                   :children [#:block{:uid "239090a3c",
                                                                                      :string "Nothing happens if you click on these links because you're already on this page.",}]}
                                                           #:block{:uid "7f087f26e",
                                                                   :string "Block references with `(())`: ((b0acdcabd))",
                                                                   :open? false,
                                                                   :children [#:block{:uid "b0acdcabd",
                                                                                      :string "I am being referenced by other blocks.",}]}
                                                           #:block{:uid "0f5b500f6",
                                                                   :string "{{[[TODO]]}} `ctrl-enter` / `cmd-enter` (for mac) to cycle between TODO and DONE",}
                                                           #:block{:uid "851cfb2f3",
                                                                   :string "embeds with `{{[[youtube: ]]}}` and `{{``iframe: }}`",
                                                                   :open? false,
                                                                   :children [#:block{:uid "d1825590b",
                                                                                      :string "{{[[youtube]]: https://www.youtube.com/watch?v=dQw4w9WgXcQ}}",}
                                                                              #:block{:uid "56771d0e4",
                                                                                      :string "{{iframe: https://www.openstreetmap.org/export/embed.html?bbox=-0.004017949104309083%2C51.47612752641776%2C0.00030577182769775396%2C51.478569861898606&layer=mapnik}}",}]}
                                                           #:block{:uid "d04604730",
                                                                   :string "images with `![]()` ![athens-splash](https://raw.githubusercontent.com/athensresearch/athens/master/doc/athens-puk-patrick-unsplash.jpg)",}
                                                           #:block{:uid "dd1e080f4"
                                                                   :string "$$\\LaTeX$$ support"
                                                                   :children [#:block{:uid "dd1e080f5"
                                                                                      :string "$$c = \\pm\\sqrt{a^2 + b^2}$$"}
                                                                              #:block{:uid "dd1e080f6"
                                                                                      :string "$$E=mc^2$$"}
                                                                              #:block{:uid "dd1e080f7"
                                                                                      :string "$$\\int_{a}^{b} x^2 \\,dx$$"}
                                                                              #:block{:uid "dd1e080f8"
                                                                                      :string "$$\\sum_{n=1}^{\\infty} 2^{-n} = 1$$"}
                                                                              #:block{:uid "dd1e080f9"
                                                                                      :string "$$\\prod_{i=a}^{b} f(i)$$"}
                                                                              #:block{:uid "dd1e080fa"
                                                                                      :string "$$\\lim_{x\\to\\infty} f(x)$$"}
                                                                              #:block{:uid "dd1e080fb"
                                                                                      :string "Highlights invalid $$\\LaTeX$$ in red, like this $$\\Latex$$"}
                                                                              #:block{:uid "dd1e080fc"
                                                                                      :string "Also `mhchem` extension is available:"
                                                                                      :children [#:block{:uid "dd1e080fd"
                                                                                                         :string "$$\\ce{Zn^2+  <=>[+ 2OH-][+ 2H+]  $\\underset{\\text{amphoteres Hydroxid}}{\\ce{Zn(OH)2 v}}$  <=>[+ 2OH-][+ 2H+]  $\\underset{\\text{Hydroxozikat}}{\\ce{[Zn(OH)4]^2-}}$}$$"}]}]}]}
                                        #:block{:uid "94272f778",
                                                :string "All Keybindings",
                                                :open? false,
                                                :children [#:block{:uid "e425468c5",
                                                                   :string "block shortcuts (while editing a block)",
                                                                   :children [#:block{:uid "0ea74b8e8",
                                                                                      :string "`ctrl-b` / `cmd-b` (for mac): **bold**",}
                                                                              #:block{:uid "b96876779",
                                                                                      :string "`/`: slash commands",}
                                                                              #:block{:uid "8ceea983f",
                                                                                      :string "`tab`: indent",}
                                                                              #:block{:uid "814db8ad5",
                                                                                      :string "`shift-tab`: unindent",}
                                                                              #:block{:uid "450028510",
                                                                                      :string "`shift-up` or `shift-down`: select multiple blocks",}
                                                                              #:block{:uid "019774c8b",
                                                                                      :string "`ctrl-a` / `cmd-a` (for mac): select all blocks on page",}
                                                                              #:block{:uid "1be25bf14",
                                                                                      :string "`ctrl-z` / `cmd-z` (for mac): undo",}
                                                                              #:block{:uid "7379f541a",
                                                                                      :string "`ctrl-shift-z` / `cmd-shift-z` (for mac): redo",}
                                                                              #:block{:uid "0c11c0416",
                                                                                      :string "`ctrl-up` / `cmd-up` or `ctrl-down` / `cmd-down`: collapse or expand blocks",}]}
                                                           #:block{:uid "311b96eb2",
                                                                   :string "global shortcuts (can use anywhere)",
                                                                   :children [#:block{:uid "55ea160af",
                                                                                      :string "`ctrl-\\` or `cmd-\\` (for mac): open left sidebar",}
                                                                              #:block{:uid "13efc72fd",
                                                                                      :string "`ctrl-shift-\\` or `cmd-shift-\\` (for mac): open right sidebar",}
                                                                              #:block{:uid "bb0e8a187",
                                                                                      :string "`ctrl-k` / `cmd-k` (for mac): open search bar",}
                                                                              #:block{:uid "13dfc72bd",
                                                                                      :string "`alt-g`: open graph view",}
                                                                              #:block{:uid "13edc72bd",
                                                                                      :string "`alt-d`: open daily note",}
                                                                              #:block{:uid "13esc72bd",
                                                                                      :string "`alt-a`: open all-pages view",}
                                                                              #:block{:uid "13efc72bd",
                                                                                      :string "`ctrl-comma` / `cmd-comma` (for mac): open settings page",}]}]}
                                        #:block{:uid "1002528bd",
                                                :string "Left Sidebar",
                                                :open? false,
                                                :children [#:block{:uid "574973f5c",
                                                                   :string "Mark a page as a shortcut with the caret next to the page title.",}]}
                                        #:block{:uid "72538ef7f",
                                                :string "Right Sidebar",
                                                :open? false,
                                                :children [#:block{:uid "9d6e1fd07",
                                                                   :string "Open a block or page in the right sidebar by shift clicking on the link, title, or bullet.",}]}]}
                     #:block{:uid "21785e1a9",
                             :string "**FAQ**",
                             :open? false,
                             :children [#:block{:uid "792717c36",
                                                :string "How does Athens persist data?",
                                                :open? false,
                                                :children [#:block{:uid "58803d15f",
                                                                   :string "Athens is persisted to your filesystem at `documents/athens` by default.",}
                                                           #:block{:uid "0f62fecbc",
                                                                   :string "Database can be changed through settings button on the top right corner.",}]}
                                        #:block{:uid "68246ce0a",
                                                :string "How can I report bugs?",
                                                :open? false,
                                                :children [#:block{:uid "37dcfbf20",
                                                                   :string "If your bug isn't already on our [GitHub Bug and Issue Board](https://github.com/athensresearch/athens/projects/4), post the bug to the beta testers Discord channel. Screenshots are particularly useful. Also post the version of Athens and Operating System you are on.",}]}
                                        #:block{:uid "9576d79db",
                                                :string "How do I update Athens?",
                                                :open? false,
                                                :children [#:block{:uid "199259bce",
                                                                   :string "When Athens is launched, it looks for newer versions. If it finds a newer version, it downloads it and launches it the next time you open Athens.",}
                                                           #:block{:uid "bf257cc8e",
                                                                   :string "You can see the version at the bottom of the left sidebar when it is opened. Click on the version to go to our [release notes on Notion](https://www.notion.so/athensresearch/Weekly-Updates-e18afa006cfd4fec9c462940ac3b84da).",}]}
                                        #:block{:uid "2464d4538",
                                                :string "Is there anything special about the [[Welcome]] page?",
                                                :open? false,
                                                :children [#:block{:uid "6275554a3",
                                                                   :string "[[Welcome]] is a special page. When you restart Athens, any changes you make to this page will be overwritten, so don't write anything you need in this page!",}]}]}]}])


(def welcome-events
  (let [op (bfs/build-paste-op common-db/empty-db welcome-page-internal-representation)
        welcome-page (common-events/build-atomic-event op)
        add-sidebar  (common-events/build-atomic-event (atomic-graph-ops/make-shortcut-new-op welcome-page-title))]
    [[(:event/id welcome-page) welcome-page]
     [(:event/id add-sidebar) add-sidebar]]))
