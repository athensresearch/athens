(ns athens.athens-datoms)

;; athens namespaced pages that are updated when app is updated
(def datoms
  [{:block/uid "0",
    :node/title "athens/Welcome",
    :page/sidebar 999,
    :block/children [#:block{:uid "a6f7b01cf",
                             :string "Welcome to Athens, Open-Source Networked Thought!",
                             :open true,
                             :order 0}
                     #:block{:uid "f5dd95e6e",
                             :string "Markup Features",
                             :open false,
                             :order 7,
                             :children [#:block{:uid "c9e48f596",
                                                :string "Bold text with **double asterisks**",
                                                :open true,
                                                :order 0}
                                        #:block{:uid "9f727fd2b",
                                                :string "Mono-spaced text with `backticks`",
                                                :open true,
                                                :order 1}
                                        #:block{:uid "5d19451db",
                                                :string "Links with `[[]]`, `#`, or `#[[]]`: [[athens/Welcome]] #athens/Welcome #[[athens/Welcome]]",
                                                :open true,
                                                :order 2}
                                        #:block{:uid "ddcf4ba1f",
                                                :string "Block references with `(())`: ((82247e489))",
                                                :open true,
                                                :order 3,
                                                :children [#:block{:uid "82247e489",
                                                                   :string "I am being referenced",
                                                                   :open true,
                                                                   :order 0,
                                                                   :_refs [#:db{:id 347}]}]}
                                        #:block{:uid "5ac7f905f",
                                                :string "{{[[TODO]]}} `ctrl-enter` to cycle between TODO and DONE",
                                                :open true,
                                                :order 4}
                                        #:block{:uid "f22247778",
                                                :string "embeds with `{{[[youtube: ]]}}` and `{{``iframe: }}`",
                                                :open false,
                                                :order 5,
                                                :children [#:block{:uid "2da5522a1",
                                                                   :string "{{[[youtube]]: https://www.youtube.com/watch?v=dQw4w9WgXcQ}}",
                                                                   :open true,
                                                                   :order 0}
                                                           #:block{:uid "50cfadc73",
                                                                   :string "{{iframe: https://www.openstreetmap.org/export/embed.html?bbox=-0.004017949104309083%2C51.47612752641776%2C0.00030577182769775396%2C51.478569861898606&layer=mapnik}}",
                                                                   :open true,
                                                                   :order 1}]}
                                        #:block{:uid "2af204111",
                                                :string "images with `![]()` ![athens-splash](https://raw.githubusercontent.com/athensresearch/athens/master/doc/athens-puk-patrick-unsplash.jpg)",
                                                :open true,
                                                :order 6}]}
                     #:block{:uid "eda8f737a",
                             :string "All Keybindings",
                             :open false,
                             :order 8,
                             :children [#:block{:uid "6684acffe",
                                                :string "local shortcuts (within a block)",
                                                :open true,
                                                :order 0,
                                                :children [#:block{:uid "19c858229",
                                                                   :string "`ctrl-b`: **bold**",
                                                                   :open true,
                                                                   :order 0}
                                                           #:block{:uid "dfc7e935e",
                                                                   :string "`/`: slash commands",
                                                                   :open true,
                                                                   :order 1}
                                                           #:block{:uid "30e54f370",
                                                                   :string "`tab`: indent",
                                                                   :open true,
                                                                   :order 2}
                                                           #:block{:uid "39e46d281",
                                                                   :string "`shift-up` or `shift-down`: select multiple blocks",
                                                                   :open true,
                                                                   :order 4}
                                                           #:block{:uid "2c3a20456",
                                                                   :string "`ctrl-z`: undo",
                                                                   :open true,
                                                                   :order 6}
                                                           #:block{:uid "e6dfe6693",
                                                                   :string "`ctrl-shift-z`: redo",
                                                                   :open true,
                                                                   :order 7}
                                                           #:block{:uid "1ecab0585",
                                                                   :string "`shift-tab`: unindent block ",
                                                                   :open true,
                                                                   :order 3}
                                                           #:block{:uid "8f5ff2896",
                                                                   :string "`ctrl-a`: select all blocks on page",
                                                                   :open true,
                                                                   :order 5}]}
                                        #:block{:uid "2960a50f4",
                                                :string "global shortcuts (can use anywhere)",
                                                :open true,
                                                :order 1,
                                                :children [#:block{:uid "33f88d8d6",
                                                                   :string "`ctrl-\\`: open left sidebar",
                                                                   :open true,
                                                                   :order 0}
                                                           #:block{:uid "72d86bbb0",
                                                                   :string "`ctrl-shift-\\`: open right sidebar",
                                                                   :open true,
                                                                   :order 1}
                                                           #:block{:uid "c993bf326",
                                                                   :string "`ctrl-k`: open search bar",
                                                                   :open true,
                                                                   :order 2}]}]}
                     #:block{:uid "020a90740",
                             :string "Left Sidebar",
                             :open true,
                             :order 9,
                             :children [#:block{:uid "a82850462",
                                                :string "Mark a page as a shortcut with the caret next to the page title.",
                                                :open true,
                                                :order 0}]}
                     #:block{:uid "539723d85",
                             :string "Right Sidebar",
                             :open true,
                             :order 10,
                             :children [#:block{:uid "4e12e40ed",
                                                :string "Open a block or page in the right sidebar by shift clicking on the title or bullet.",
                                                :open true,
                                                :order 0}]}
                     #:block{:uid "a0b16ab19",
                             :string "Outliner Features",
                             :open false,
                             :order 6,
                             :children [#:block{:uid "d6c47a7f4",
                                                :string "Indent and unindent bullets with tab and shift-tab.",
                                                :open true,
                                                :order 0}
                                        #:block{:uid "2f53541d7",
                                                :string "Drag and drop bullets to re-order blocks.",
                                                :open true,
                                                :order 1}
                                        #:block{:uid "41a752cb5",
                                                :string "Select multiple bullets with click and drag or shift-up or shift-down.",
                                                :open true,
                                                :order 2}]}
                     #:block{:uid "3938f6d7b",
                             :string "Athens is persisted to your filesystem at `documents/athens`. Soon you will be able to choose any location for your db (including Dropbox folders).",
                             :open true,
                             :order 2}
                     #:block{:uid "9f8187d34",
                             :string "You can connect pages together with bi-directional links. You are currently on [[athens/Welcome]]. If you go to [[athens/Changelog]], you will see this block in \"Linked References\".",
                             :open true,
                             :order 1}
                     #:block{:uid "6de8b7d13",
                             :string "[[athens/Welcome]] and [[athens/Changelog]] are reserved pages. When a new version of Athens is deployed, your app will update automatically. These pages will be updated as well. Any changes you make to these pages will be overwritten, so don't write anything you need in these pages!",
                             :open true,
                             :order 3}
                     #:block{:uid "5c872acd9",
                             :string "You can click on a bullet [insert ascii bullet] to zoom-in on it.",
                             :open true,
                             :order 5,
                             :children [#:block{:uid "f0bad2f38",
                                                :string "If a block has a `>` or `v` to the left of its bullet, that means it has children. You can click the `v` and `>` to open and close the bullet.",
                                                :open true,
                                                :order 0}]}
                     #:block{:uid "365c52fff", :string "", :open true, :order 4}]}
   {:node/title     "athens/Changelog",
    :block/children [{:block/string   "[[September 29, 2020]]",
                      :block/children [{:block/string "The beginning of the in-Athens Changelog.",
                                        :block/uid    "8eb0523bd",
                                        :block/open   true,
                                        :block/order  0}],
                      :block/uid      "52604194d",
                      :block/open     true,
                      :block/order    0}],
    :block/uid      "1",
    :page/sidebar   1000}])
