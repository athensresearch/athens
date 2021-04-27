(ns athens.athens-datoms)

;; Reserved pages that are updated when app is loaded.
(def datoms
  [{:block/uid "0",
    :node/title "Welcome",
    :page/sidebar 999,
    :block/children [#:block{:uid "ee770c334",
                             :string "Welcome to Athens, Open-Source Networked Thought!",
                             :open true,
                             :order 0}
                     #:block{:uid "6aecd4172",
                             :string "You can open and close blocks that have children.",
                             :open true,
                             :order 1,
                             :children [#:block{:uid "5f82a48ef",
                                                :string "![](https://athens-assets-1.s3.us-east-2.amazonaws.com/welcome.gif)",
                                                :open true,
                                                :order 0}]}
                     #:block{:uid "7e409b1cb",
                             :string "**How to Use Athens**",
                             :open false,
                             :order 2,
                             :children [#:block{:uid "289cc9981",
                                                :string "Outliner Features",
                                                :open false,
                                                :order 0,
                                                :children [#:block{:uid "62b9428d5",
                                                                   :string "You can click on a bullet • to zoom in on it.",
                                                                   :open true,
                                                                   :order 0,
                                                                   :children [#:block{:uid "70907b596",
                                                                                      :string "You can navigate back to a higher context by clicking on navigation breadcrumbs (when zoomed in).",
                                                                                      :open true,
                                                                                      :order 0}]}
                                                           #:block{:uid "c312c0f9a",
                                                                   :string "Indent and unindent bullets with tab and shift-tab.",
                                                                   :open true,
                                                                   :order 1}
                                                           #:block{:uid "59ccb6c73",
                                                                   :string "Drag and drop bullets to re-order blocks.",
                                                                   :open true,
                                                                   :order 2}
                                                           #:block{:uid "a8589c828",
                                                                   :string "Select multiple bullets with click and drag or shift-up or shift-down.",
                                                                   :open true,
                                                                   :order 3,
                                                                   :children [#:block{:uid "6cf8e69b2",
                                                                                      :string "You can drag and drop with multiple blocks selected!",
                                                                                      :open true,
                                                                                      :order 0}]}]}
                                        #:block{:uid "6b8c28b09",
                                                :string "Markup Features",
                                                :open false,
                                                :order 1,
                                                :children [#:block{:uid "e434db606",
                                                                   :string "To edit the raw-text of a block, simply click on it and begin typing!",
                                                                   :open true,
                                                                   :order 0}
                                                           #:block{:uid "e5dec8a28",
                                                                   :string "Bold text with **double asterisks**",
                                                                   :open true,
                                                                   :order 1}
                                                           #:block{:uid "3949afab9",
                                                                   :string "Mono-spaced text with `backticks`",
                                                                   :open true,
                                                                   :order 2}
                                                           #:block{:uid "a8760ca6d",
                                                                   :string "Links with `[[]]`, `#`, or `#[[]]`: [[Welcome]] #Welcome #[[Welcome]]",
                                                                   :open false,
                                                                   :order 3,
                                                                   :children [#:block{:uid "239090a3c",
                                                                                      :string "Nothing happens if you click on these links because you're already on this page.",
                                                                                      :open true,
                                                                                      :order 0}]}
                                                           #:block{:uid "7f087f26e",
                                                                   :string "Block references with `(())`: ((b0acdcabd))",
                                                                   :open false,
                                                                   :order 4,
                                                                   :children [#:block{:uid "b0acdcabd",
                                                                                      :string "I am being referenced by other blocks.",
                                                                                      :open true,
                                                                                      :order 0,
                                                                                      :_refs []}]}
                                                           #:block{:uid "0f5b500f6",
                                                                   :string "{{[[TODO]]}} `ctrl-enter` to cycle between TODO and DONE",
                                                                   :open true,
                                                                   :order 5}
                                                           #:block{:uid "851cfb2f3",
                                                                   :string "embeds with `{{[[youtube: ]]}}` and `{{``iframe: }}`",
                                                                   :open false,
                                                                   :order 6,
                                                                   :children [#:block{:uid "d1825590b",
                                                                                      :string "{{[[youtube]]: https://www.youtube.com/watch?v=dQw4w9WgXcQ}}",
                                                                                      :open true,
                                                                                      :order 0}
                                                                              #:block{:uid "56771d0e4",
                                                                                      :string "{{iframe: https://www.openstreetmap.org/export/embed.html?bbox=-0.004017949104309083%2C51.47612752641776%2C0.00030577182769775396%2C51.478569861898606&layer=mapnik}}",
                                                                                      :open true,
                                                                                      :order 1}]}
                                                           #:block{:uid "d04604730",
                                                                   :string "images with `![]()` ![athens-splash](https://raw.githubusercontent.com/athensresearch/athens/master/doc/athens-puk-patrick-unsplash.jpg)",
                                                                   :open true,
                                                                   :order 7}
                                                           #:block{:uid "dd1e080f4"
                                                                   :string "$$\\LaTeX$$ support"
                                                                   :open true
                                                                   :order 8
                                                                   :children [#:block{:uid "dd1e080f5"
                                                                                      :string "$$c = \\pm\\sqrt{a^2 + b^2}$$"
                                                                                      :open true
                                                                                      :order 0}
                                                                              #:block{:uid "dd1e080f6"
                                                                                      :string "$$E=mc^2$$"
                                                                                      :open true
                                                                                      :order 1}
                                                                              #:block{:uid "dd1e080f7"
                                                                                      :string "$$\\int_{a}^{b} x^2 \\,dx$$"
                                                                                      :open true
                                                                                      :order 2}
                                                                              #:block{:uid "dd1e080f8"
                                                                                      :string "$$\\sum_{n=1}^{\\infty} 2^{-n} = 1$$"
                                                                                      :open true
                                                                                      :order 3}
                                                                              #:block{:uid "dd1e080f9"
                                                                                      :string "$$\\prod_{i=a}^{b} f(i)$$"
                                                                                      :open true
                                                                                      :order 4}
                                                                              #:block{:uid "dd1e080fa"
                                                                                      :string "$$\\lim_{x\\to\\infty} f(x)$$"
                                                                                      :open true
                                                                                      :order 5}
                                                                              #:block{:uid "dd1e080fb"
                                                                                      :string "Highlights invalid $$\\LaTeX$$ in red, like this $$\\Latex$$"
                                                                                      :open true
                                                                                      :order 6}
                                                                              #:block{:uid "dd1e080fc"
                                                                                      :string "Also `mhchem` extension is available:"
                                                                                      :open true
                                                                                      :order 7
                                                                                      :children [#:block{:uid "dd1e080fd"
                                                                                                         :string "$$\\ce{Zn^2+  <=>[+ 2OH-][+ 2H+]  $\\underset{\\text{amphoteres Hydroxid}}{\\ce{Zn(OH)2 v}}$  <=>[+ 2OH-][+ 2H+]  $\\underset{\\text{Hydroxozikat}}{\\ce{[Zn(OH)4]^2-}}$}$$"
                                                                                                         :open true
                                                                                                         :order 0}]}]}]}
                                        #:block{:uid "94272f778",
                                                :string "All Keybindings",
                                                :open false,
                                                :order 2,
                                                :children [#:block{:uid "e425468c5",
                                                                   :string "block shortcuts (while editing a block)",
                                                                   :open true,
                                                                   :order 0,
                                                                   :children [#:block{:uid "0ea74b8e8",
                                                                                      :string "`ctrl-b`: **bold**",
                                                                                      :open true,
                                                                                      :order 0}
                                                                              #:block{:uid "b96876779",
                                                                                      :string "`/`: slash commands",
                                                                                      :open true,
                                                                                      :order 1}
                                                                              #:block{:uid "8ceea983f",
                                                                                      :string "`tab`: indent",
                                                                                      :open true,
                                                                                      :order 2}
                                                                              #:block{:uid "814db8ad5",
                                                                                      :string "`shift-tab`: unindent",
                                                                                      :open true,
                                                                                      :order 3}
                                                                              #:block{:uid "450028510",
                                                                                      :string "`shift-up` or `shift-down`: select multiple blocks",
                                                                                      :open true,
                                                                                      :order 4}
                                                                              #:block{:uid "019774c8b",
                                                                                      :string "`ctrl-a`: select all blocks on page",
                                                                                      :open true,
                                                                                      :order 5}
                                                                              #:block{:uid "1be25bf14",
                                                                                      :string "`ctrl-z`: undo",
                                                                                      :open true,
                                                                                      :order 6}
                                                                              #:block{:uid "7379f541a",
                                                                                      :string "`ctrl-shift-z`: redo",
                                                                                      :open true,
                                                                                      :order 7}
                                                                              #:block{:uid "0c11c0416",
                                                                                      :string "`ctrl-up` or `ctrl-down`: collapse or expand blocks",
                                                                                      :open true,
                                                                                      :order 8}]}
                                                           #:block{:uid "311b96eb2",
                                                                   :string "global shortcuts (can use anywhere)",
                                                                   :open true,
                                                                   :order 1,
                                                                   :children [#:block{:uid "55ea160af",
                                                                                      :string "`ctrl-\\`: open left sidebar",
                                                                                      :open true,
                                                                                      :order 0}
                                                                              #:block{:uid "13efc72bd",
                                                                                      :string "`ctrl-shift-\\`: open right sidebar",
                                                                                      :open true,
                                                                                      :order 1}
                                                                              #:block{:uid "bb0e8a187",
                                                                                      :string "`ctrl-k`: open search bar",
                                                                                      :open true,
                                                                                      :order 2}]}]}
                                        #:block{:uid "1002528bd",
                                                :string "Left Sidebar",
                                                :open false,
                                                :order 3,
                                                :children [#:block{:uid "574973f5c",
                                                                   :string "Mark a page as a shortcut with the caret next to the page title.",
                                                                   :open true,
                                                                   :order 0}]}
                                        #:block{:uid "72538ef7f",
                                                :string "Right Sidebar",
                                                :open false,
                                                :order 4,
                                                :children [#:block{:uid "9d6e1fd07",
                                                                   :string "Open a block or page in the right sidebar by shift clicking on the link, title, or bullet.",
                                                                   :open true,
                                                                   :order 0}]}]}
                     #:block{:uid "21785e1a9",
                             :string "**FAQ**",
                             :open false,
                             :order 3,
                             :children [#:block{:uid "792717c36",
                                                :string "How does Athens persist data?",
                                                :open false,
                                                :order 0,
                                                :children [#:block{:uid "58803d15f",
                                                                   :string "Athens is persisted to your filesystem at `documents/athens` by default.",
                                                                   :open true,
                                                                   :order 0}
                                                           #:block{:uid "0f62fecbc",
                                                                   :string "Database can be changed through settings button on the top right corner.",
                                                                   :open true,
                                                                   :order 1}]}
                                        #:block{:uid "68246ce0a",
                                                :string "How can I report bugs?",
                                                :open false,
                                                :order 1,
                                                :children [#:block{:uid "37dcfbf20",
                                                                   :string "If your bug isn't already on our [GitHub Bug and Issue Board](https://github.com/athensresearch/athens/projects/4), post the bug to the beta testers Discord channel. Screenshots are particularly useful. Also post the version of Athens and Operating System you are on.",
                                                                   :open true,
                                                                   :order 0}]}
                                        #:block{:uid "9576d79db",
                                                :string "How do I update Athens?",
                                                :open false,
                                                :order 2,
                                                :children [#:block{:uid "199259bce",
                                                                   :string "When Athens is launched, it looks for newer versions. If it finds a newer version, it downloads it and launches it the next time you open Athens.",
                                                                   :open true,
                                                                   :order 0}
                                                           #:block{:uid "bf257cc8e",
                                                                   :string "You can see the version at the bottom of the left sidebar when it is opened. Click on the version to go to our [release notes on Notion](https://www.notion.so/athensresearch/Weekly-Updates-e18afa006cfd4fec9c462940ac3b84da).",
                                                                   :open true,
                                                                   :order 1}]}
                                        #:block{:uid "2464d4538",
                                                :string "Is there anything special about the [[Welcome]] page?",
                                                :open false,
                                                :order 3,
                                                :children [#:block{:uid "6275554a3",
                                                                   :string "[[Welcome]] is a special page. When you restart Athens, any changes you make to this page will be overwritten, so don't write anything you need in this page!",
                                                                   :open true,
                                                                   :order 0}]}]}]}])

