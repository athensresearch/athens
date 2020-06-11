(ns athens.devcards.blocks
  (:require
    [athens.devcards.db :refer [new-conn posh-conn! load-real-db-button]]
    [athens.db]
    [athens.lib.dom.attributes :refer [with-styles with-attributes]]
    [athens.parse-renderer :refer [parse-and-render]]
    [athens.patterns :as patterns]
    [athens.router :refer [navigate-page toggle-open]]
    [athens.style :refer [style-guide-css +flex-column +flex-center]]
    [cljsjs.react]
    [cljsjs.react.dom]
    [devcards.core :refer-macros [defcard defcard-rg]]
    ["@material-ui/icons" :as mui-icons]
    [posh.reagent :refer [transact! pull pull-many q]]
    [reagent.core :as r]
    [re-frame.core :refer [dispatch]]))


(defcard-rg Import-Styles
  [style-guide-css])


(defcard Instantiate-Dsdb)
(defonce conn (new-conn))
(posh-conn! conn)

(comment
  [{:db/id        4291,
    :block/uid    "0MtCtwFh0",
    :create/email "tangj1122@gmail.com",
    :create/time  1587924500189,
    :edit/email   "tangj1122@gmail.com",
    :edit/time    1587924500192,
    :node/title   "Datomic"}
   {:block/string "[[[[Datomic]]: [[Event Sourcing]] without the hassle]]",
    :create/email "tangj1122@gmail.com",
    :create/time  1587924394698,
    :block/refs   4292,
    :block/uid    "-ejAtqgis",
    :block/open   true,
    :edit/time    1587924500188,
    :db/id        4286,
    :edit/email   "tangj1122@gmail.com",
    :block/order  1}
   {:block/string "there is no hard limit, but don't put 100 billion datoms in Datomic",
    :create/email "tangj1122@gmail.com",
    :create/time  1587680559303,
    :block/uid    "6S4eVeXo8",
    :block/open   true,
    :edit/time    1587680599692,
    :db/id        3673,
    :edit/email   "tangj1122@gmail.com",
    :block/order  1}
   {:block/string "**there are only 4 people doing all of the dev on Datomic, Clojure**, etc combined so we are a tiny team of very experienced people using high leverage tools. I'm not sure this is directly relevant to most software teams in general (but Clojure projects do probably tend to be more that, and less big teams)",
    :create/email "tangj1122@gmail.com",
    :create/time  1588171319980,
    :block/uid    "zm_Ft2Iim",
    :block/open   true,
    :edit/time    1588171339086,
    :db/id        5008,
    :edit/email   "tangj1122@gmail.com",
    :block/order  3}
   {:db/id          1
    :block/uid      "uid1",
    :node/title     "top-level page"
    :block/children [{:db/id          2
                      :block/uid      "uid2",
                      :block/open     true,
                      :block/string   "child block - uid2"
                      :block/children [{:db/id        4
                                        :block/uid    "uid4"
                                        :block/string "child block - uid4"}]}
                     {:db/id          3
                      :block/uid      "uid3"
                      :block/open     false
                      :block/string   "child block - uid3"
                      :block/children [{:db/id        5
                                        :block/uid    "uid5"
                                        :block/string "child block - uid5"}]}]}])

(defcard-rg Create-Datoms
  (let [datoms [{:db/id 2381,
                 :node/title "Athens FAQ",
                 :block/uid "OaSVyM_nr",
                 :block/children [{:db/id 2135,
                                   :block/string "Why Clojure?",
                                   :block/uid "gEDJF5na2",
                                   :block/children [{:db/id 2384,
                                                     :block/string "For a deeper breakdown of the technology [[Athens vs Roam Tech Stack]]",
                                                     :block/uid "3eptV2Zpm",
                                                     :block/open true}
                                                    {:db/id 2387,
                                                     :block/string "Clojure is great, read [[Why you should learn Clojure - my first month as a Clojurian]]",
                                                     :block/uid "42KTGQUyp",
                                                     :block/open true}
                                                    {:db/id 3040,
                                                     :block/string "Ensures possibility of feature parity with Roam.",
                                                     :block/uid "GZLRVsreB",
                                                     :block/children [{:db/id 4397,
                                                                       :block/string "While Clojure is not necessary to develop an application, an application that promises a knowledge graph probably should be built off of a graph database.",
                                                                       :block/uid "lxMRAb5Y5",
                                                                       :block/open true}],
                                                     :block/open true}],
                                   :block/open true}
                                  {:db/id 2158,
                                   :block/string "Why open-source?",
                                   :block/uid "BjIm6GeRP",
                                   :block/children [{:db/id 2163,
                                                     :block/string "The short answer is the security and privacy of your data.",
                                                     :block/uid "GNaf3XzpE",
                                                     :block/open true}
                                                    {:db/id 2347,
                                                     :block/string "Firstly, I wouldn't be surprised if Roam was eventually open-sourced.",
                                                     :block/uid "jbiKpcmIX",
                                                     :block/children [{:db/id 2176,
                                                                       :block/string "Suffice it to say that Roam being open-source is undeniably something that the team has already considered. Why is it not open-source already? You'd have to ask the Roam team, but Roam, a business, is not obligated to open-source anything.",
                                                                       :block/uid "gVINXaN8Y",
                                                                       :block/open true}
                                                                      {:db/id 2346,
                                                                       :block/string "The conclusion of the [[Roam White Paper]] states that Roam's vision is a collective, \"open-source\" intelligence.",
                                                                       :block/uid "ZOxwo0K_7",
                                                                       :block/children [{:db/id 2174,
                                                                                         :block/string "((iWmBJaChO))",
                                                                                         :block/uid "WKWPPSYQa",
                                                                                         :block/open true}],
                                                                       :block/open true}
                                                                      {:db/id 2349,
                                                                       :block/string "In the Roam Slack, I recall Conor saying one eventual goal is to work on a protocol that affords interoperability between open source alternatives. I would share the message but can't find it because of Slack's 10k message limit.",
                                                                       :block/uid "VQ-ybRmNh",
                                                                       :block/open true}
                                                                      {:db/id 2351,
                                                                       :block/string "Ultimately, we don't know when/if Roam will be open-sourced, but it's possible that Athens could accelerate or catalyze this. Regardless, there will always be some who are open-source maximalists and some who want to self-host, because that's probably really the most secure thing you can do (if you know what you're doing).",
                                                                       :block/uid "PGGS8MFH_",
                                                                       :block/open true}],
                                                     :block/open true}
                                                    {:db/id 2364,
                                                     :block/string "The longer answer is that I believe the humble link is arguably the most important protocol to the Web itself. Even if Roam doesn't become open-source, we need to be thinking about bi-directional links as an open standard more deeply and publicly. #Hyperlink",
                                                     :block/uid "6oHUcLKYA",
                                                     :block/children [{:db/id 2350,
                                                                       :block/string "The link is the fundamental parameter that drives the most valuable algorithm in the world, Google's PageRank.",
                                                                       :block/uid "rtNqzJU10",
                                                                       :block/open true}
                                                                      {:db/id 2355,
                                                                       :block/string "[[Venkatesh Rao]], in [[The Rhetoric of the Hyperlink]], writes: ((gHCKfrghZ))",
                                                                       :block/uid "FBSTouuY2",
                                                                       :block/open true}
                                                                      {:db/id 2366,
                                                                       :block/string "[[James P. Carse]], in [[Finite and Infinite Games]], writes: ((9wSe1KotV))",
                                                                       :block/uid "QLhYQnUyA",
                                                                       :block/open true}
                                                                      {:db/id 2372,
                                                                       :block/string "The Internet is a __network__ of computers. Society is a __network__ of humans. But unlike computers, there is no \"atomic\" individual. We are defined necessarily through our relationships.",
                                                                       :block/uid "tR2Wna0ir",
                                                                       :block/open true}
                                                                      {:db/id 2374,
                                                                       :block/string "I hope Athens can play some role in answering the question of how we can design an open protocol for bi-directional links.",
                                                                       :block/uid "iD4dVwEIR",
                                                                       :block/open true}
                                                                      {:db/id 2375,
                                                                       :block/string "And there is no better time than quaran-time to be reimagining the fabric of our social infrastructure, particularly of what I believe is the most important infrastructure of society today: the Web and the Internet more broadly.",
                                                                       :block/uid "aK4gKd6Eq",
                                                                       :block/open true}
                                                                      {:db/id 2377,
                                                                       :block/string "If you are interested in this conversation, join us in the #athens channel of the Roam Slack or ping me on Twitter.",
                                                                       :block/uid "e6K3mCb74",
                                                                       :block/open true}
                                                                      {:db/id 2378,
                                                                       :block/string "I'm not a protocol-designer by any means, but I do think we are standing on the shoulders of giants with regards to the [IETF's RFCs](https://en.wikipedia.org/wiki/List_of_RFCs) and the [[Semantic Web]]. More recently, we've seen a renaissance in open protocol design from the blockchain world. [Drachma](https://en.wikipedia.org/wiki/Greek_drachma) ICO anyone?",
                                                                       :block/uid "L5GIcpfst",
                                                                       :block/open true}
                                                                      {:db/id 2392,
                                                                       :block/string "So let's imagine",
                                                                       :block/uid "Sb9neCBj1",
                                                                       :block/children [{:db/id 2164,
                                                                                         :block/string "I can see `<a href=\"www.twitter.com/tangjeff0\" **bidirectional**>Jeff's Twitter</a>` being interesting, `<a href=\"www.twitter.com\" **bidirectional**>Twitter</a>` not so much. The same goes for blogging and social networks generally.",
                                                                                         :block/uid "Rah4b1g0Z",
                                                                                         :block/open true}
                                                                                        {:db/id 2167,
                                                                                         :block/string "What if iMessage, Gmail, and [insert any other app] had bi-directional links?",
                                                                                         :block/uid "xYunO5c0w",
                                                                                         :block/children [{:db/id 2168,
                                                                                                           :block/string "More interestingly, what if they had bi-directional links between one another?",
                                                                                                           :block/uid "39RZ5buhi",
                                                                                                           :block/open true}],
                                                                                         :block/open true}],
                                                                       :block/open true}],
                                                     :block/open true}],
                                   :block/open true}
                                  {:db/id 2390,
                                   :block/string "What's the roadmap?",
                                   :block/uid "6mgsvrfj9",
                                   :block/children [{:db/id 2391,
                                                     :block/string "[[Create a Minimal Viable Alternative to Roam]]",
                                                     :block/uid "wzJ0kQzXX",
                                                     :block/open true}
                                                    {:db/id 2393,
                                                     :block/string "Design and implement a cloud hosted Athens",
                                                     :block/uid "S94gjS2Ig",
                                                     :block/open true}
                                                    {:db/id 2394,
                                                     :block/string "Design and implement a React Native mobile client",
                                                     :block/uid "Ip9U2KEdq",
                                                     :block/open true}
                                                    {:db/id 2395,
                                                     :block/string "[[Begin RFCs for an open protocol for bi-directional links]] that affords interopability between Roam, Athens, and other applications",
                                                     :block/uid "VF-u1hbXF",
                                                     :block/children [{:db/id 2396,
                                                                       :block/string "((L5GIcpfst))",
                                                                       :block/uid "200PVRGaK",
                                                                       :block/open true}],
                                                     :block/open true}],
                                   :block/open true}
                                  {:db/id 2401, :block/string "type:: [[documentation]]", :block/uid "6f_4BReoO", :block/open true}
                                  {:db/id 3026,
                                   :block/string "Github: https://github.com/athensresearch/athens",
                                   :block/uid "HJMBcfwRz",
                                   :block/open true}
                                  {:db/id 3029,
                                   :block/string "Roam Slack invite link (join the #athens channel): https://roamresearch.slack.com/join/shared_invite/enQtODg3NjIzODEwNDgwLTdhMjczMGYwN2YyNmMzMDcyZjViZDk0MTA2M2UxOGM5NTMxNDVhNDE1YWVkNTFjMGM4OTE3MTQ3MjEzNzE1MTA",
                                   :block/uid "8mZgP0oYu",
                                   :block/open true}
                                  {:db/id 4391,
                                   :block/string "Have you heard about X?",
                                   :block/uid "kbrRsiO53",
                                   :block/children [{:db/id 4392,
                                                     :block/string "There are many other great projects in this space! Checkout:",
                                                     :block/uid "w1yDyW7CD",
                                                     :block/children [{:db/id 4393,
                                                                       :block/string "[org-roam](https://github.com/jethrokuan/org-roam)",
                                                                       :block/uid "XT8svnebx",
                                                                       :block/open true}
                                                                      {:db/id 4394,
                                                                       :block/string "[TiddlyRoam](https://joekroese.github.io/tiddlyroam/)",
                                                                       :block/uid "NZXZR4v6y",
                                                                       :block/open true}
                                                                      {:db/id 4395,
                                                                       :block/string "",
                                                                       :block/uid "8Zu7tDRus",
                                                                       :block/open true}
                                                                      {:db/id 4396,
                                                                       :block/string "https://nesslabs.com/roam-research-alternatives",
                                                                       :block/uid "fhL48iiBy",
                                                                       :block/open true}],
                                                     :block/open true}
                                                    {:db/id 4401,
                                                     :block/string "((lxMRAb5Y5))",
                                                     :block/uid "OeDMpsJPo",
                                                     :block/open true}],
                                   :block/open true}],
                 :block/open true}
                {:db/id 4093,
                 :node/title "Hyperlink",
                 :block/uid "p1Xv2crs3",
                 :block/children [{:db/id 4095,
                                   :block/string "https://en.wikipedia.org/wiki/Hyperlink",
                                   :block/uid "VzPuJjfd2",
                                   :block/children [{:db/id 4094,
                                                     :block/string "In some hypertext, hyperlinks can be bidirectional: they can be followed in two directions, so both ends act as anchors and as targets. More complex arrangements exist, such as many-to-many links.",
                                                     :block/uid "ekpvuMWbj",
                                                     :block/open true}
                                                    {:db/id 4096,
                                                     :block/string "The effect of following a hyperlink may vary with the hypertext system and may sometimes depend on the link itself; for instance, on the World Wide Web most hyperlinks cause the target document to replace the document being displayed, but some are marked to cause the target document to open in a new window (or, perhaps, in a new tab[2]). Another possibility is [[transclusion]], for which the link target is a document fragment that replaces the link anchor within the source document. Not only persons browsing the document follow hyperlinks. These hyperlinks may also be followed automatically by programs. A program that traverses the hypertext, following each hyperlink and gathering all the retrieved documents is known as a Web spider or crawler.",
                                                     :block/uid "sXHI5FK64",
                                                     :block/open true}
                                                    {:db/id 4097,
                                                     :block/string "A **fat link** (also known as a \"one-to-many\" link, an \"extended link\"[4]) or a \"multi-tailed link\" [5] is a **hyperlink which leads to multiple endpoints**; the link is a multivalued function.",
                                                     :block/uid "RrYc_7MPT",
                                                     :block/open true}
                                                    {:db/id 4099,
                                                     :block/string "Webgraph is a graph, formed from web pages as vertices and hyperlinks, as directed edges.",
                                                     :block/uid "YDDdtGRlI",
                                                     :block/open true}
                                                    {:db/id 4100,
                                                     :block/string "**Permalinks** are URLs that are intended to remain unchanged for many years into the future, yielding hyperlink that are less susceptible to **link rot**. Permalinks are often rendered simply, that is, as friendly URLs, so as to be easy for people to type and remember. Permalinks are used in order to point and redirect readers to the same Web page, blog post or any online digital media[9].",
                                                     :block/uid "-egRK52sJ",
                                                     :block/open true}
                                                    {:db/id 4101,
                                                     :block/string "The scientific literature is a place where link persistence is crucial to the public knowledge. A 2013 study in BMC Bioinformatics analyzed 15,000 links in abstracts from Thomson Reuters’ Web of Science citation index, founding that **the median lifespan of Web pages was 9.3 years, and just 62% were archived**.[10] The median lifespan of a Web page constitutes high-degree variable, but its order of magnitude usually is of some months.[11]",
                                                     :block/uid "Dc63ZgNzb",
                                                     :block/open true}
                                                    {:db/id 4103,
                                                     :block/string "It uses the HTML element \"a\" with the attribute \"href\" (HREF is an abbreviation for \"**Hypertext REFerence**\"[12]) and optionally also the attributes \"title\", \"target\", and \"class\" or \"id\"",
                                                     :block/uid "eqEXIPpP9",
                                                     :block/open true}
                                                    {:db/id 4104,
                                                     :block/string "The first widely used open protocol that included hyperlinks from any Internet site to any other Internet site was the **Gopher protocol** from 1991. It was soon eclipsed by HTML after the 1993 release of the [[Mosaic]] browser (which could handle Gopher links as well as HTML links). HTML's advantage was the ability to mix graphics, text, and hyperlinks, unlike Gopher, which just had menu-structured text and hyperlinks",
                                                     :block/uid "shz0NlaMi",
                                                     :block/open true}
                                                    {:db/id 4105,
                                                     :block/string " database program [[HyperCard]] was released in 1987 for the Apple Macintosh that allowed hyperlinking between various pages within a document, and was probably the first use of the word \"hyperlink",
                                                     :block/uid "kqTCXm_yU",
                                                     :block/open true}
                                                    {:db/id 4107,
                                                     :block/string "While hyperlinking among webpages is an intrinsic feature of the web, some websites object to being linked by other websites; some have claimed that linking to them is not allowed without permission.",
                                                     :block/uid "Wt3tKYZDA",
                                                     :block/open true}],
                                   :block/open false}
                                  {:db/id 4110,
                                   :block/string "https://en.wikipedia.org/wiki/Backlink",
                                   :block/uid "H0FXP0c3Q",
                                   :block/children [{:db/id 4111,
                                                     :block/string "**Backlinks are offered in Wikis, but usually only within the bounds of the Wiki itself and enabled by the database backend. **MediaWiki, specifically offers the \"What links here\" tool, some older Wikis, especially the first WikiWikiWeb, had the backlink functionality exposed in the page title.",
                                                     :block/uid "fng0afASL",
                                                     :block/open true}
                                                    {:db/id 4112,
                                                     :block/string "Search engines often use the number of backlinks that a website has as one of the most important factors for determining that website's search engine ranking, popularity and importance. Google's description of its PageRank system, for instance, notes that \"Google interprets a link from page A to page B as a vote, by page A, for page B.",
                                                     :block/uid "FpUyVEMN1",
                                                     :block/open true}],
                                   :block/open true}
                                  {:db/id 4118,
                                   :block/string "Linkback",
                                   :block/uid "JGR1uZgy0",
                                   :block/children [{:db/id 4119,
                                                     :block/string "A linkback is a method for Web authors to obtain notifications when other authors link to one of their documents. This enables authors to keep track of who is linking to, or referring to, their articles. The four methods (Refback, Trackback, Pingback and Webmention) differ in how they accomplish this task.",
                                                     :block/uid "C6FO9Giqn",
                                                     :block/open true}],
                                   :block/open true}
                                  {:db/id 4120,
                                   :block/string "Trackback",
                                   :block/uid "wS-AtXyHn",
                                   :block/children [{:db/id 4121,
                                                     :block/string "A trackback is an acknowledgment. This acknowledgment is sent via a network signal (XML-RPC ping) from the originating site to the receiving site. The receptor often publishes a link back to the originator indicating its worthiness. **Trackback requires both sites to be trackback-enabled in order to establish this communication.**",
                                                     :block/uid "hOCOOluZ8",
                                                     :block/open true}
                                                    {:db/id 4122,
                                                     :block/string "Some individuals or companies have abused the TrackBack feature to insert spam links on some blogs. This is similar to comment spam but avoids some of the safeguards designed to stop the latter practice. As a result, TrackBack spam filters similar to those implemented against comment spam now exist in many weblog publishing systems. Many blogs have stopped using trackbacks because dealing with spam became too much of a burden",
                                                     :block/uid "_avg0uZuz",
                                                     :block/open true}],
                                   :block/open true}
                                  {:db/id 4123,
                                   :block/string "Pingback",
                                   :block/uid "JBFk5Uc7r",
                                   :block/children [{:db/id 4124,
                                                     :block/string "In March 2014, Akamai published a report about a widely seen exploit involving Pingback that targets vulnerable WordPress sites.[1] This exploit led to massive abuse of legitimate blogs and websites and turned them into unwilling participants in a DDoS attack.[2] Details about this vulnerability have been publicized since 2012.[3]",
                                                     :block/uid "XyesVee2k",
                                                     :block/open true}],
                                   :block/open true}
                                  {:db/id 4126,
                                   :block/string "Webmention",
                                   :block/uid "ni62Bz4oU",
                                   :block/children [{:db/id 4127,
                                                     :block/string "Similar to pingback, Webmention is one of four types of linkbacks, but was designed to be simpler than the XML-RPC protocol that pingback relies upon, by instead only using HTTP and x-www-urlencoded content.[2]. Beyond previous linkback protocols, Webmention also specifies protocol details for when a page that is the source of a link is deleted, or updated with new links or removal of existing links",
                                                     :block/uid "cU-OjOmW8",
                                                     :block/open true}
                                                    {:db/id 4128,
                                                     :block/string "published as a W3C working draft on January 12, 2016.[3] As of January 12, 2017 it is a W3C recommendation",
                                                     :block/uid "z54AwF39K",
                                                     :block/open true}],
                                   :block/open true}
                                  {:db/id 4129,
                                   :block/string "Refback",
                                   :block/uid "SXpTdaKTw",
                                   :block/children [{:db/id 4130,
                                                     :block/string "A Refback is simply the usage of the HTTP referrer header to discover incoming links. Whenever a browser traverses an incoming link from Site A (originator) to Site B (receptor) the browser will send a referrer value indicating the URL from where the user came. Site B might publish a link to Site A after visiting Site A and extracting relevant information from Site A such as the title, meta information, the link text, and so on.[1]

                                                    ",
                                                     :block/uid "KIgUMwz54",
                                                     :block/open true}
                                                    {:db/id 4135, :block/string "", :block/uid "Bvexgultr", :block/open true}],
                                   :block/open true}
                                  {:db/id 4136, :block/string "type:: notes", :block/uid "7ZHM9WBJ4", :block/open true}
                                  {:db/id 4137, :block/string "", :block/uid "YDTpf-rMy", :block/open true}],
                 :block/open true}]]
    [:button.primary {:on-click #(transact! conn datoms)} "Create Datoms"]))


(defcard-rg Load-Real-DB
  [load-real-db-button conn])


(defn toggle
  [dbid open?]
  (transact! conn [{:db/id dbid :block/open (not open?)}]))


(def +gray-circle
  (with-styles +flex-center
    {:height 12 :width 12 :margin-right 5 :margin-top 5 :border-radius "50%" :cursor "pointer"}))


(def +black-circle
  (with-styles {:height 5 :width 5 :border-radius "50%" :cursor "pointer" :display "inline-block"
                :background-color "black" :vertical-align "middle"}))


(comment
  "Playing around with using MaterialUI icons for circles instead of plain CSS"
  [:div +flex-center
   [:> mui-icons/FiberManualRecord (with-styles {:font-size 10 :color "lightgray"})]
   [:> mui-icons/FiberManualRecord (with-styles {:font-size 6})]])

(defn sort-block
  [block]
  (if-let [children (seq (:block/children block))]
    (assoc block :block/children
                 (sort-by :block/order (map sort-block children)))
    block))


(declare block-component)

(defn block-el [block]
  "Two checks to make sure block is open or not: children exist and :block/open bool"
  (let [{:block/keys [uid string open children] dbid :db/id} block
        open? (and (seq children) open)
        closed? (and (seq children) (not open))]
    [:div +flex-column
     [:div {:style {:display "flex"}}
      (cond
        open? [:> mui-icons/KeyboardArrowRight {:style {:cursor "pointer"} :on-click #(toggle dbid open)}]
        closed? [:> mui-icons/KeyboardArrowDown {:style {:cursor "pointer"} :on-click #(toggle dbid open)}]
        :else [:span {:style {:width 10}}])
      [:span (with-styles +gray-circle {:background-color (if closed? "lightgray" nil)})
       [:span (with-attributes +black-circle {:on-click #(navigate-page uid)})]]
      [:span string]
      ;; TODO parse-and-render will break because it uses rfee/href
      ;;[:span (parse-and-render string)]
      ]
     (when open?
       (for [child (:block/children block)]
         [:div {:style {:margin-left 28} :key (:db/id child)}
          [block-el child]]))]))


(defn block-component [ident]
  "This query is long because I'm not sure how to recursively find all child blocks with all attributes
  '[* {:block/children [*]}] doesn't work
Also, why does datascript return a reaction of {:db/id nil} when pulling for [:block/uid uid]?
no results for q returns nil
no results for pull eid returns nil
  "
  (fn []
    (let [block (pull conn '[:db/id :block/string :block/uid :block/children :block/open {:block/children ...}] ident)]
      (when (:db/id @block)
        [block-el @block]))))


(def enter-keycode 13)
(def esc-keycode 27)


(defn title-comp [title]
  (let [s (r/atom {:editing false
                         :current-title title})
        save! (fn [new-title]
                (swap! s assoc :editing false)
                ;;(dispatch [:node/renamed (:current-title @s) new-title])
                )
        cancel! (fn [] (swap! s assoc :editing false))]
    (fn [title]
      (if (:editing @s)
        [:input {:default-value title
                 :auto-focus true
                 :on-blur #(save! (-> % .-target .-value))
                 :on-key-down #(cond
                                 (= (.-keyCode %) enter-keycode)
                                 (save! (-> % .-target .-value))

                                 (= (.-keyCode %) esc-keycode)
                                 (cancel!)

                                 :else nil)}]
        [:h2 {:on-click (fn [_] (swap! s #(-> %
                                            (assoc :editing true)
                                            (assoc :current-title title))))}
         title]))))


(defn merge-prompt
  "probably want to allow global Alert to accept arbitrary hiccup
  (when (get @merge :active false)\n    [merge-prompt @merge])"
  [{:keys [old-title new-title]}]
  [:div {:style {:background "red" :color "white"}}
   (str "\"" new-title "\" already exists, merge pages?")
   [:a {:on-click #(dispatch [:node/merged old-title new-title])
        :style {:margin-left "30px"}}
    "yes"]
   [:a {:on-click #(dispatch [:node/merge-canceled])
        :style {:margin-left "30px"}}
    "no"]])

(defn node-page-el [node linked-refs unlinked-refs]
  (let [{:keys [block/children node/title]} node]
    [:div
     [title-comp title]
     [:div
      (for [child children]
        ^{:key (:db/id child)} [block-el child])]
     [:div
      [:h4 "Linked References"]
      (for [ref linked-refs]
        ^{:key ref} [:p ref])]
     [:div
      [:h4 "Unlinked References"]
      (for [ref unlinked-refs]
        ^{:key ref} [:p ref])]]))


(def q-refs
  '[:find [?e ...]
    :in $ ?regex
    :where
    [?e :block/string ?s]
    [(re-find ?regex ?s)]])


(defn node-page-component
  "One diff between datascript and posh: we don't have pull in q for posh
  https://github.com/mpdairy/posh/issues/21"
  [ident]
  (fn []
    (let [node          (pull conn '[:db/id :node/title :block/string :block/uid :block/children :block/open {:block/children ...}] ident) ;; TODO: pull recursively
          title         (:node/title @node)
          linked-refs   (q q-refs conn (patterns/linked title))
          unlinked-refs (q q-refs conn (patterns/unlinked title))]
      (prn @node)
      (when @node
        [node-page-el @node @linked-refs @unlinked-refs]))))

;;  TODO: Will be broken as long as we are using `rfee/href` to link to pages."
;; TODO we shouldn't query for (un)linked refs if the query fails
;; but it should never fail?

(defcard-rg Node-Page
  "pull entity 4093: \"Hyperlink\" page"
  [node-page-component 4093])

(defn shape-parent-query
  "Find path from nested block to origin node.
  Again, don't understand why query returns {:db/id nil} if no query. Why not just nil?"
  [pull-results]
  (if-not (:db/id pull-results)
    (vector)
    (->> (loop [b   pull-results
                res []]
           (if (:node/title b)
             (conj res b)
             (recur (first (:block/_children b))
               (conj res (dissoc b :block/_children)))))
      (rest)
      (reverse))))


(defn block-page-el
  [block parents]
  (let [{:block/keys [string children]} block]
    [:div
     [:span {:style {:color "gray"}}
      (comment
        create an interpose function that can take [:hiccup elements]
        can probably just map over the interpose, replacing " > " with [:hiccup vec])
      (interpose
        " > "
        (for [p parents]
          (let [{:keys [node/title block/uid block/string]} p]
            [:span {:key uid :style {:cursor "pointer"} :on-click #(navigate-page uid)} (or string title)])))
      ]
     [:h2 (str "• " string)]
     [:div (for [child children]
        (let [{:keys [db/id]} child]
          ^{:key id} [block-el child])
        )]
     ]))

(defn block-page-component
  "two queries: block+children and parents"
  [ident]
  (let [block @(pull conn '[:db/id :block/uid :block/string :block/open {:block/children ...}] ident)
        parents (->> @(pull conn '[:db/id :node/title :block/uid :block/string {:block/_children ...}] ident)
                  (shape-parent-query))]
    (block-page-el block parents)))


(defcard-rg Block-Page
  [block-page-component 2347])


;;(defn page-component
;;  []
;;  (fn []
;;    (let [current-route (subscribe [:current-route])
;;          uid (-> @current-route :path-params :id)
;;          node-or-block @(pull conn '[*] [:block/uid uid])]
;;      [:div {:style {:margin-left "40px" :margin-right "40px"}}
;;       (if (:node/title node-or-block)
;;         [node-page-component (:db/id node-or-block)]
;;         [block-page-component (:db/id node-or-block)]
;;         )])))
