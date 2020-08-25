(ns athens.devcards
  (:require
    [athens.db :refer [dsdb]]
    [athens.devcards.alerts]
    [athens.devcards.all-pages]
    [athens.devcards.athena]
    [athens.devcards.block-page]
    [athens.devcards.blocks]
    [athens.devcards.breadcrumbs]
    [athens.devcards.buttons]
    [athens.devcards.daily-notes]
    [athens.devcards.db]
    [athens.devcards.db-boxes]
    [athens.devcards.devtool]
    [athens.devcards.dropdown]
    [athens.devcards.filters]
    [athens.devcards.icons]
    [athens.devcards.left-sidebar]
    [athens.devcards.node-page]
    [athens.devcards.parser]
    [athens.devcards.right-sidebar]
    [athens.devcards.sci-boxes]
    [athens.devcards.spinner]
    [athens.devcards.style-guide]
    [athens.devcards.styling-with-stylefy]
    [athens.devcards.textinput]
    [athens.effects]
    [athens.events]
    [athens.listeners :as listeners]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core]
    [posh.reagent :refer [transact!]]
    [re-frame.core :refer [dispatch-sync]]
    [stylefy.core :as stylefy]))


;; Mock Data

(def athens-faq
  {:db/id          2381,
   :block/uid      "OaSVyM_nr",
   :block/open     true,
   :node/title     "Athens FAQ",
   :block/children [{:db/id          2135,
                     :block/uid      "gEDJF5na2",
                     :block/string   "Why Clojure?",
                     :block/open     true,
                     :block/order    4,
                     :block/children [{:db/id        2384,
                                       :block/uid    "3eptV2Zpm",
                                       :block/string "For a deeper breakdown of the technology [[Athens vs Roam Tech Stack]]",
                                       :block/open   true,
                                       :block/order  1}
                                      {:db/id        2387,
                                       :block/uid    "42KTGQUyp",
                                       :block/string "Clojure is great, read [[Why you should learn Clojure - my first month as a Clojurian]]",
                                       :block/open   true,
                                       :block/order  2}
                                      {:db/id          3040,
                                       :block/uid      "GZLRVsreB",
                                       :block/string   "Ensures possibility of feature parity with Roam.",
                                       :block/open     true,
                                       :block/order    0,
                                       :block/children [{:db/id        4397,
                                                         :block/uid    "lxMRAb5Y5",
                                                         :block/string "While Clojure is not necessary to develop an application, an application that promises a knowledge graph probably should be built off of a graph database.",
                                                         :block/open   true,
                                                         :block/order  0}]}]}
                    {:db/id          2158,
                     :block/uid      "BjIm6GeRP",
                     :block/string   "Why open-source?",
                     :block/open     true,
                     :block/order    3,
                     :block/children [{:db/id        2163,
                                       :block/uid    "GNaf3XzpE",
                                       :block/string "The short answer is the security and privacy of your data.",
                                       :block/open   true,
                                       :block/order  1}
                                      {:db/id          2347,
                                       :block/uid      "jbiKpcmIX",
                                       :block/string   "Firstly, I wouldn't be surprised if Roam was eventually open-sourced.",
                                       :block/open     true,
                                       :block/order    0,
                                       :block/children [{:db/id        2176,
                                                         :block/uid    "gVINXaN8Y",
                                                         :block/string "Suffice it to say that Roam being open-source is undeniably something that the team has already considered. Why is it not open-source already? You'd have to ask the Roam team, but Roam, a business, is not obligated to open-source anything.",
                                                         :block/open   true,
                                                         :block/order  2}
                                                        {:db/id          2346,
                                                         :block/uid      "ZOxwo0K_7",
                                                         :block/string   "The conclusion of the [[Roam White Paper]] states that Roam's vision is a collective, \"open-source\" intelligence.",
                                                         :block/open     true,
                                                         :block/order    0,
                                                         :block/children [{:db/id        2174,
                                                                           :block/uid    "WKWPPSYQa",
                                                                           :block/string "((iWmBJaChO))",
                                                                           :block/open   true,
                                                                           :block/order  0}]}
                                                        {:db/id        2349,
                                                         :block/uid    "VQ-ybRmNh",
                                                         :block/string "In the Roam Slack, I recall Conor saying one eventual goal is to work on a protocol that affords interoperability between open source alternatives. I would share the message but can't find it because of Slack's 10k message limit.",
                                                         :block/open   true,
                                                         :block/order  1}
                                                        {:db/id        2351,
                                                         :block/uid    "PGGS8MFH_",
                                                         :block/string "Ultimately, we don't know when/if Roam will be open-sourced, but it's possible that Athens could accelerate or catalyze this. Regardless, there will always be some who are open-source maximalists and some who want to self-host, because that's probably really the most secure thing you can do (if you know what you're doing).",
                                                         :block/open   true,
                                                         :block/order  3}]}
                                      {:db/id          2364,
                                       :block/uid      "6oHUcLKYA",
                                       :block/string   "The longer answer is that I believe the humble link is arguably the most important protocol to the Web itself. Even if Roam doesn't become open-source, we need to be thinking about bi-directional links as an open standard more deeply and publicly. #Hyperlink",
                                       :block/open     true,
                                       :block/order    2,
                                       :block/children [{:db/id        2350,
                                                         :block/uid    "rtNqzJU10",
                                                         :block/string "The link is the fundamental parameter that drives the most valuable algorithm in the world, Google's PageRank.",
                                                         :block/open   true,
                                                         :block/order  0}
                                                        {:db/id        2355,
                                                         :block/uid    "FBSTouuY2",
                                                         :block/string "[[Venkatesh Rao]], in [[The Rhetoric of the Hyperlink]], writes: ((gHCKfrghZ))",
                                                         :block/open   true,
                                                         :block/order  1}
                                                        {:db/id        2366,
                                                         :block/uid    "QLhYQnUyA",
                                                         :block/string "[[James P. Carse]], in [[Finite and Infinite Games]], writes: ((9wSe1KotV))",
                                                         :block/open   true,
                                                         :block/order  2}
                                                        {:db/id        2372,
                                                         :block/uid    "tR2Wna0ir",
                                                         :block/string "The Internet is a __network__ of computers. Society is a __network__ of humans. But unlike computers, there is no \"atomic\" individual. We are defined necessarily through our relationships.",
                                                         :block/open   true,
                                                         :block/order  3}
                                                        {:db/id        2374,
                                                         :block/uid    "iD4dVwEIR",
                                                         :block/string "I hope Athens can play some role in answering the question of how we can design an open protocol for bi-directional links.",
                                                         :block/open   true,
                                                         :block/order  4}
                                                        {:db/id        2375,
                                                         :block/uid    "aK4gKd6Eq",
                                                         :block/string "And there is no better time than quaran-time to be reimagining the fabric of our social infrastructure, particularly of what I believe is the most important infrastructure of society today: the Web and the Internet more broadly.",
                                                         :block/open   true,
                                                         :block/order  5}
                                                        {:db/id        2377,
                                                         :block/uid    "e6K3mCb74",
                                                         :block/string "If you are interested in this conversation, join us in the #athens channel of the Roam Slack or ping me on Twitter.",
                                                         :block/open   true,
                                                         :block/order  8}
                                                        {:db/id        2378,
                                                         :block/uid    "L5GIcpfst",
                                                         :block/string "I'm not a protocol-designer by any means, but I do think we are standing on the shoulders of giants with regards to the [IETF's RFCs](https://en.wikipedia.org/wiki/List_of_RFCs) and the [[Semantic Web]]. More recently, we've seen a renaissance in open protocol design from the blockchain world. [Drachma](https://en.wikipedia.org/wiki/Greek_drachma) ICO anyone?",
                                                         :block/open   true,
                                                         :block/order  7}
                                                        {:db/id          2392,
                                                         :block/uid      "Sb9neCBj1",
                                                         :block/string   "So let's imagine",
                                                         :block/open     true,
                                                         :block/order    6,
                                                         :block/children [{:db/id        2164,
                                                                           :block/uid    "Rah4b1g0Z",
                                                                           :block/string "I can see `<a href=\"www.twitter.com/tangjeff0\" **bidirectional**>Jeff's Twitter</a>` being interesting, `<a href=\"www.twitter.com\" **bidirectional**>Twitter</a>` not so much. The same goes for blogging and social networks generally.",
                                                                           :block/open   true,
                                                                           :block/order  0}
                                                                          {:db/id          2167,
                                                                           :block/uid      "xYunO5c0w",
                                                                           :block/string   "What if iMessage, Gmail, and [insert any other app] had bi-directional links?",
                                                                           :block/open     true,
                                                                           :block/order    1,
                                                                           :block/children [{:db/id        2168,
                                                                                             :block/uid    "39RZ5buhi",
                                                                                             :block/string "More interestingly, what if they had bi-directional links between one another?",
                                                                                             :block/open   true,
                                                                                             :block/order  0}]}]}]}]}
                    {:db/id          2390,
                     :block/uid      "6mgsvrfj9",
                     :block/string   "What's the roadmap?",
                     :block/open     true,
                     :block/order    5,
                     :block/children [{:db/id        2391,
                                       :block/uid    "wzJ0kQzXX",
                                       :block/string "[[Create a Minimal Viable Alternative to Roam]]",
                                       :block/open   true,
                                       :block/order  0}
                                      {:db/id        2393,
                                       :block/uid    "S94gjS2Ig",
                                       :block/string "Design and implement a cloud hosted Athens",
                                       :block/open   true,
                                       :block/order  1}
                                      {:db/id        2394,
                                       :block/uid    "Ip9U2KEdq",
                                       :block/string "Design and implement a React Native mobile client",
                                       :block/open   true,
                                       :block/order  2}
                                      {:db/id          2395,
                                       :block/uid      "VF-u1hbXF",
                                       :block/string   "[[Begin RFCs for an open protocol for bi-directional links]] that affords interopability between Roam, Athens, and other applications",
                                       :block/open     true,
                                       :block/order    3,
                                       :block/children [{:db/id        2396,
                                                         :block/uid    "200PVRGaK",
                                                         :block/string "((L5GIcpfst))",
                                                         :block/open   true,
                                                         :block/order  0}]}]}
                    {:db/id        2401,
                     :block/uid    "6f_4BReoO",
                     :block/string "type:: [[documentation]]",
                     :block/open   true,
                     :block/order  0}
                    {:db/id        3026,
                     :block/uid    "HJMBcfwRz",
                     :block/string "Github: https://github.com/athensresearch/athens",
                     :block/open   true,
                     :block/order  1}
                    {:db/id        3029,
                     :block/uid    "8mZgP0oYu",
                     :block/string "Roam Slack invite link (join the #athens channel): https://roamresearch.slack.com/join/shared_invite/enQtODg3NjIzODEwNDgwLTdhMjczMGYwN2YyNmMzMDcyZjViZDk0MTA2M2UxOGM5NTMxNDVhNDE1YWVkNTFjMGM4OTE3MTQ3MjEzNzE1MTA",
                     :block/open   true,
                     :block/order  2}
                    {:db/id          4391,
                     :block/uid      "kbrRsiO53",
                     :block/string   "Have you heard about X?",
                     :block/open     true,
                     :block/order    6,
                     :block/children [{:db/id          4392,
                                       :block/uid      "w1yDyW7CD",
                                       :block/string   "There are many other great projects in this space! Checkout:",
                                       :block/open     true,
                                       :block/order    0,
                                       :block/children [{:db/id        4393,
                                                         :block/uid    "XT8svnebx",
                                                         :block/string "[org-roam](https://github.com/jethrokuan/org-roam)",
                                                         :block/open   true,
                                                         :block/order  1}
                                                        {:db/id        4394,
                                                         :block/uid    "NZXZR4v6y",
                                                         :block/string "[TiddlyRoam](https://joekroese.github.io/tiddlyroam/)",
                                                         :block/open   true,
                                                         :block/order  2}
                                                        {:db/id        4395,
                                                         :block/uid    "8Zu7tDRus",
                                                         :block/string "",
                                                         :block/open   true,
                                                         :block/order  2}
                                                        {:db/id        4396,
                                                         :block/uid    "fhL48iiBy",
                                                         :block/string "https://nesslabs.com/roam-research-alternatives",
                                                         :block/open   true,
                                                         :block/order  3}]}
                                      {:db/id        4401,
                                       :block/uid    "OeDMpsJPo",
                                       :block/string "((lxMRAb5Y5))",
                                       :block/open   true,
                                       :block/order  1}]}]})


(def hyperlink
  {:db/id          4093,
   :block/uid      "p1Xv2crs3",
   :block/open     true,
   :node/title     "Hyperlink",
   :block/children [{:db/id          4095,
                     :block/uid      "VzPuJjfd2",
                     :block/string   "https://en.wikipedia.org/wiki/Hyperlink",
                     :block/open     false,
                     :block/order    2,
                     :block/children [{:db/id        4094,
                                       :block/uid    "ekpvuMWbj",
                                       :block/string "In some hypertext, hyperlinks can be bidirectional: they can be followed in two directions, so both ends act as anchors and as targets. More complex arrangements exist, such as many-to-many links.",
                                       :block/open   true,
                                       :block/order  0}
                                      {:db/id        4096,
                                       :block/uid    "sXHI5FK64",
                                       :block/string "The effect of following a hyperlink may vary with the hypertext system and may sometimes depend on the link itself; for instance, on the World Wide Web most hyperlinks cause the target document to replace the document being displayed, but some are marked to cause the target document to open in a new window (or, perhaps, in a new tab[2]). Another possibility is [[transclusion]], for which the link target is a document fragment that replaces the link anchor within the source document. Not only persons browsing the document follow hyperlinks. These hyperlinks may also be followed automatically by programs. A program that traverses the hypertext, following each hyperlink and gathering all the retrieved documents is known as a Web spider or crawler.",
                                       :block/open   true,
                                       :block/order  1}
                                      {:db/id        4097,
                                       :block/uid    "RrYc_7MPT",
                                       :block/string "A **fat link** (also known as a \"one-to-many\" link, an \"extended link\"[4]) or a \"multi-tailed link\" [5] is a **hyperlink which leads to multiple endpoints**; the link is a multivalued function.",
                                       :block/open   true,
                                       :block/order  2}
                                      {:db/id        4099,
                                       :block/uid    "YDDdtGRlI",
                                       :block/string "Webgraph is a graph, formed from web pages as vertices and hyperlinks, as directed edges.",
                                       :block/open   true,
                                       :block/order  3}
                                      {:db/id        4100,
                                       :block/uid    "-egRK52sJ",
                                       :block/string "**Permalinks** are URLs that are intended to remain unchanged for many years into the future, yielding hyperlink that are less susceptible to **link rot**. Permalinks are often rendered simply, that is, as friendly URLs, so as to be easy for people to type and remember. Permalinks are used in order to point and redirect readers to the same Web page, blog post or any online digital media[9].",
                                       :block/open   true,
                                       :block/order  4}
                                      {:db/id        4101,
                                       :block/uid    "Dc63ZgNzb",
                                       :block/string "The scientific literature is a place where link persistence is crucial to the public knowledge. A 2013 study in BMC Bioinformatics analyzed 15,000 links in abstracts from Thomson Reuters’ Web of Science citation index, founding that **the median lifespan of Web pages was 9.3 years, and just 62% were archived**.[10] The median lifespan of a Web page constitutes high-degree variable, but its order of magnitude usually is of some months.[11]",
                                       :block/open   true,
                                       :block/order  5}
                                      {:db/id        4103,
                                       :block/uid    "eqEXIPpP9",
                                       :block/string "It uses the HTML element \"a\" with the attribute \"href\" (HREF is an abbreviation for \"**Hypertext REFerence**\"[12]) and optionally also the attributes \"title\", \"target\", and \"class\" or \"id\"",
                                       :block/open   true,
                                       :block/order  6}
                                      {:db/id        4104,
                                       :block/uid    "shz0NlaMi",
                                       :block/string "The first widely used open protocol that included hyperlinks from any Internet site to any other Internet site was the **Gopher protocol** from 1991. It was soon eclipsed by HTML after the 1993 release of the [[Mosaic]] browser (which could handle Gopher links as well as HTML links). HTML's advantage was the ability to mix graphics, text, and hyperlinks, unlike Gopher, which just had menu-structured text and hyperlinks",
                                       :block/open   true,
                                       :block/order  8}
                                      {:db/id        4105,
                                       :block/uid    "kqTCXm_yU",
                                       :block/string " database program [[HyperCard]] was released in 1987 for the Apple Macintosh that allowed hyperlinking between various pages within a document, and was probably the first use of the word \"hyperlink",
                                       :block/open   true,
                                       :block/order  7}
                                      {:db/id        4107,
                                       :block/uid    "Wt3tKYZDA",
                                       :block/string "While hyperlinking among webpages is an intrinsic feature of the web, some websites object to being linked by other websites; some have claimed that linking to them is not allowed without permission.",
                                       :block/open   true,
                                       :block/order  9}]}
                    {:db/id          4110,
                     :block/uid      "H0FXP0c3Q",
                     :block/string   "https://en.wikipedia.org/wiki/Backlink",
                     :block/open     true,
                     :block/order    3,
                     :block/children [{:db/id        4111,
                                       :block/uid    "fng0afASL",
                                       :block/string "**Backlinks are offered in Wikis, but usually only within the bounds of the Wiki itself and enabled by the database backend. **MediaWiki, specifically offers the \"What links here\" tool, some older Wikis, especially the first WikiWikiWeb, had the backlink functionality exposed in the page title.",
                                       :block/open   true,
                                       :block/order  0}
                                      {:db/id        4112,
                                       :block/uid    "FpUyVEMN1",
                                       :block/string "Search engines often use the number of backlinks that a website has as one of the most important factors for determining that website's search engine ranking, popularity and importance. Google's description of its PageRank system, for instance, notes that \"Google interprets a link from page A to page B as a vote, by page A, for page B.",
                                       :block/open   true,
                                       :block/order  1}]}
                    {:db/id          4118,
                     :block/uid      "JGR1uZgy0",
                     :block/string   "Linkback",
                     :block/open     true,
                     :block/order    4,
                     :block/children [{:db/id        4119,
                                       :block/uid    "C6FO9Giqn",
                                       :block/string "A linkback is a method for Web authors to obtain notifications when other authors link to one of their documents. This enables authors to keep track of who is linking to, or referring to, their articles. The four methods (Refback, Trackback, Pingback and Webmention) differ in how they accomplish this task.",
                                       :block/open   true,
                                       :block/order  0}]}
                    {:db/id          4120,
                     :block/uid      "wS-AtXyHn",
                     :block/string   "Trackback",
                     :block/open     true,
                     :block/order    5,
                     :block/children [{:db/id        4121,
                                       :block/uid    "hOCOOluZ8",
                                       :block/string "A trackback is an acknowledgment. This acknowledgment is sent via a network signal (XML-RPC ping) from the originating site to the receiving site. The receptor often publishes a link back to the originator indicating its worthiness. **Trackback requires both sites to be trackback-enabled in order to establish this communication.**",
                                       :block/open   true,
                                       :block/order  0}
                                      {:db/id        4122,
                                       :block/uid    "_avg0uZuz",
                                       :block/string "Some individuals or companies have abused the TrackBack feature to insert spam links on some blogs. This is similar to comment spam but avoids some of the safeguards designed to stop the latter practice. As a result, TrackBack spam filters similar to those implemented against comment spam now exist in many weblog publishing systems. Many blogs have stopped using trackbacks because dealing with spam became too much of a burden",
                                       :block/open   true,
                                       :block/order  1}]}
                    {:db/id          4123,
                     :block/uid      "JBFk5Uc7r",
                     :block/string   "Pingback",
                     :block/open     true,
                     :block/order    6,
                     :block/children [{:db/id        4124,
                                       :block/uid    "XyesVee2k",
                                       :block/string "In March 2014, Akamai published a report about a widely seen exploit involving Pingback that targets vulnerable WordPress sites.[1] This exploit led to massive abuse of legitimate blogs and websites and turned them into unwilling participants in a DDoS attack.[2] Details about this vulnerability have been publicized since 2012.[3]",
                                       :block/open   true,
                                       :block/order  0}]}
                    {:db/id          4126,
                     :block/uid      "ni62Bz4oU",
                     :block/string   "Webmention",
                     :block/open     true,
                     :block/order    7,
                     :block/children [{:db/id        4127,
                                       :block/uid    "cU-OjOmW8",
                                       :block/string "Similar to pingback, Webmention is one of four types of linkbacks, but was designed to be simpler than the XML-RPC protocol that pingback relies upon, by instead only using HTTP and x-www-urlencoded content.[2]. Beyond previous linkback protocols, Webmention also specifies protocol details for when a page that is the source of a link is deleted, or updated with new links or removal of existing links",
                                       :block/open   true,
                                       :block/order  0}
                                      {:db/id        4128,
                                       :block/uid    "z54AwF39K",
                                       :block/string "published as a W3C working draft on January 12, 2016.[3] As of January 12, 2017 it is a W3C recommendation",
                                       :block/open   true,
                                       :block/order  1}]}
                    {:db/id          4129,
                     :block/uid      "SXpTdaKTw",
                     :block/string   "Refback",
                     :block/open     true,
                     :block/order    8,
                     :block/children [{:db/id        4130,
                                       :block/uid    "KIgUMwz54",
                                       :block/string "A Refback is simply the usage of the HTTP referrer header to discover incoming links. Whenever a browser traverses an incoming link from Site A (originator) to Site B (receptor) the browser will send a referrer value indicating the URL from where the user came. Site B might publish a link to Site A after visiting Site A and extracting relevant information from Site A such as the title, meta information, the link text, and so on.[1]

                                                    ",
                                       :block/open   true,
                                       :block/order  0}
                                      {:db/id        4135,
                                       :block/uid    "Bvexgultr",
                                       :block/string "",
                                       :block/open   true,
                                       :block/order  1}]}
                    {:db/id 4136, :block/uid "7ZHM9WBJ4", :block/string "type:: notes", :block/open true, :block/order 0}
                    {:db/id 4137, :block/uid "YDTpf-rMy", :block/string "", :block/open true, :block/order 1}]})


(transact! dsdb [athens-faq hyperlink])


(defn ^:export main
  []
  (stylefy/init)
  (listeners/init)
  (dispatch-sync [:init-rfdb])
  (dispatch-sync [:loading/unset])
  (devcards.core/start-devcard-ui!))
