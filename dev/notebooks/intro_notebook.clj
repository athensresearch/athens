;; # Hello, Clerk 👋
^{:nextjournal.clerk/visibility #{:hide-ns}}
(ns intro-notebook
  (:require [clojure.java.io :as io]
            [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as v]
            #_[meta-csv.core :as csv])
  #_(:import (clojure.java.io file)
           #_(java.net.http HttpRequest HttpClient HttpResponse$BodyHandlers)
           #_(java.net URI)))


;; Clerk enables a *rich*, local-first notebook experience using
;; standard Clojure namespaces and Markdown files with Clojure code
;; fences. You bring your own editor and workflow, your own
;; interactive computing habits, and Clerk enhances all of that with
;; literate programming and rich visualizations.

;; Inside `clj` files, comment blocks are interpreted as prose written
;; in an extended dialect of Markdown. Clerk supports inline TeX, so
;; we can insert the [Euler–Lagrange equation](https://en.wikipedia.org/wiki/Euler–Lagrange_equation)
;; quite easily:

;; $${\frac{d}{d t} \frac{∂ L}{∂ \dot{q}}}-\frac{∂ L}{∂ q}=0.$$

;; When Clerk interprets an `md` file, the relationship between code
;; blocks and prose is reversed. Instead of the file being code by
;; default with prose in comment blocks, it will be treated as
;; Markdown by default with Clojure code in code fences. Clerk's code
;; fences have a twist, though: they evaluate their contents.

;; There are loads of other goodies to share, most of which we'll see
;; a bit farther down the page.

;; ## Operation

;; You can load, evaluate, and present a file with the `clerk/show!`
;; function, but in most cases it's easier to start a file watcher
;; with something like:

^{:nextjournal.clerk/visibility #{:hide}}
(clerk/code '(clerk/serve! {:watch-paths ["dev/notebooks"]}))


;; ... which will automatically reload and re-eval any `clj` or `md`
;; files that change, displaying the most recently changed one in your
;; browser.

;; To make this performant enough to feel good, Clerk caches the
;; computations it performs while evaluating each file. Likewise, to
;; make sure it doesn't send too much data to the browser at once,
;; Clerk paginates data structures within an interactive viewer.

;; ## Pagination

;; As an example, the infinite sequence returned by `(range)` will be
;; loaded a little bit at a time as you click on the results. (Note
;; the little underscore under the first paren, it lets you switch
;; this sequence to a vertical rather than horizontal view).

(range)


;; Opaque objects are printed as they would be in the Clojure REPL,
;; like so:
(def notebooks
  (io/file "dev/notebooks/notebooks.clj"))


;; You can leave a form at the top-level like this to examine the
;; result of evaluating it, though you'd probably use your live
;; programming environment to do this most of the time.
(into #{} (map str) (file-seq notebooks))


;; Sometimes you don't want Clerk to cache a form. You can turn off
;; caching for a form by placing a special piece of metadata before
;; it, like this:
^:nextjournal.clerk/no-cache (shuffle (range 100))

#_"TODO show hiding forms and results"


;; Another useful technique is to put an instant marking the last time
;; a form was run. This way you can update this result at any time by
;; updating the instant.
#_(let [last-run #inst "2021-12-01T16:40:56.048896Z"] ; TODO broken?
  (shuffle (range 100)))


;; Like other objects, `UUID`s and `inst`s are rendered as they would
;; be in the REPL.
(take 10
      (repeatedly (fn []
                    {:name (str
                             (rand-nth ["Oscar" "Karen" "Vlad" "Rebecca" "Conrad"]) " "
                             (rand-nth ["Miller" "Stasčnyk" "Ronin" "Meyer" "Black"]))
                     :role (rand-nth [:admin :operator :manager :programmer :designer])
                     :id (java.util.UUID/randomUUID)
                     :created-at #inst "2021"})))


;; Clerk also supports unicode, of course.
{:hello "👋 world" :tacos (map (comp #(map (constantly '🌮) %) range) (range 1 100))}


;; ## 👁 Clerk Viewer API

;; In addition to these basic viewers for Clojure data structures,
;; Clerk comes with a set of built-in viewers for many kinds of
;; things, and a moldable viewer API that can be extended while you
;; work.

;; ### 🧩 Built-in Viewers

;; #### 🔢 Data Tables

;; Clerk provides a built-in data table viewer that supports the three
;; most common tabular data shapes out of the box: a sequence of maps,
;; where each map's keys are column names; a seq of seq, which is just
;; a grid of values with an optional header; a map of seqs, in with
;; keys are column names and rows are the values for that column.

;; NB: removed to avoid meta-csv dependency.
#_(clerk/table
 (csv/read-csv "https://gist.githubusercontent.com/netj/8836201/raw/6f9306ad21398ea43cba4f7d537619d0e07d5ae3/iris.csv"))


;; #### 📊 Plotly

;; Clerk also has built-in support for Plotly's low-ceremony plotting:
(clerk/plotly {:data [{:z [[1 2 3] [3 2 1]] :type "surface"}]})


;; #### 📈 Vega Lite

;; But Clerk also has Vega Lite for those who prefer that grammar.
(clerk/vl {:width 650
           :height 400
           :data {:url "https://vega.github.io/vega-datasets/data/us-10m.json"
                  :format {:type "topojson" :feature "counties"}}
           :transform [{:lookup "id"
                        :from {:data {:url "https://vega.github.io/vega-datasets/data/unemployment.tsv"}
                               :key "id"
                               :fields ["rate"]}}]
           :projection {:type "albersUsa"}
           :mark "geoshape"
           :encoding {:color {:field "rate"
                              :type "quantitative"}}})


;; #### 📑 Markdown

;; The same Markdown support Clerk uses for comment blocks is also
;; available programmatically:
(clerk/md (clojure.string/join "\n" (map #(str %1 ". " %2) (range 1 4) ["Lambda" "Eval" "Apply"])))

#_ "TODO numbered list style is missing numbers?"


;; #### 🤖 Code

;; There's a code viewer uses that
;; [clojure-mode](https://nextjournal.github.io/clojure-mode/) for
;; syntax highlighting.
(clerk/code (macroexpand '(when test
                            expression-1
                            expression-2)))


;; #### 🧮 TeX

;; As we've already seen, all comment blocks can contain TeX (we use
;; [KaTeX](https://katex.org/) under the covers). In addition, you can
;; call the TeX viewer programmatically. Here, for example, are
;; Maxwell's equations in differential form:
(clerk/tex "
\\begin{alignedat}{2}
  \\nabla\\cdot\\vec{E} = \\frac{\\rho}{\\varepsilon_0} & \\qquad \\text{Gauss' Law} \\\\
  \\nabla\\cdot\\vec{B} = 0 & \\qquad \\text{Gauss' Law ($\\vec{B}$ Fields)} \\\\
  \\nabla\\times\\vec{E} = -\\frac{\\partial \\vec{B}}{\\partial t} & \\qquad \\text{Faraday's Law} \\\\
  \\nabla\\times\\vec{B} = \\mu_0\\vec{J}+\\mu_0\\varepsilon_0\\frac{\\partial\\vec{E}}{\\partial t} & \\qquad \\text{Ampere's Law}
\\end{alignedat}
")


;; #### 🕸 Hiccup

;; The `html` viewer interprets `hiccup` when passed a vector. (This
;; can be quite useful for building arbitrary layouts in your
;; notebooks.)
(clerk/html [:table
             [:tr [:td "◤"] [:td "◥"]]
             [:tr [:td "◉"] [:td "◉"]]
             [:tr [:td "◣"] [:td "◢"]]])


;; Alternatively you can also just pass an HTML string, perhaps
;; generated by your code:
(clerk/html "“A brilliant solution to the wrong problem can be worse than no solution at all. Solve the correct problem.”<br/>—<em>Donald Norman</em>")


;; ### 🚀 Extensibility

;; In addition to these defaults, you can also attach a custom viewer
;; to any form. Here we make our own little viewer to greet James
;; Clerk Maxwell:
(clerk/with-viewer #(v/html [:div "Greetings to " [:strong %] "!"])
                   "James Clerk Maxwell")


;; But we can do more interesting things, like using a predicate
;; function to match numbers and turn them into headings, or
;; converting string into paragraphs.
(clerk/with-viewers [{:pred number?
                      :render-fn #(v/html [(keyword (str "h" %)) (str "Heading " %)])}
                     {:pred string?
                      :render-fn #(v/html [:p %])}]
                    [1 "To begin at the beginning:"
                     2 "It is Spring, moonless night in the small town, starless and bible-black,"
                     3 "the cobblestreets silent and the hunched,"
                     4 "courters'-and- rabbits' wood limping invisible"
                     5 "down to the sloeblack, slow, black, crowblack, fishingboat-bobbing sea."])


;; Or you could use black and white squares to render numbers:
^::clerk/no-cache
(clerk/with-viewers [{:pred number?
                      :render-fn #(v/html [:div.inline-block {:style {:width 16 :height 16}
                                                              :class (if (pos? %) "bg-black" "bg-white border-solid border-2 border-black")}])}]
                    (take 10 (repeatedly #(rand-int 2))))


;; Or build your own colour parser and then use it to generate swatches:
(clerk/with-viewers
  [{:pred #(and (string? %)
                (re-matches
                  (re-pattern
                    (str "(?i)"
                         "(#(?:[0-9a-f]{2}){2,4}|(#[0-9a-f]{3})|"
                         "(rgb|hsl)a?\\((-?\\d+%?[,\\s]+){2,3}\\s*[\\d\\.]+%?\\))")) %))
    :render-fn #(v/html [:div.inline-block.rounded-sm.shadow
                         {:style {:width 16
                                  :height 16
                                  :border "1px solid rgba(0,0,0,.2)"
                                  :background-color %}}])}]
  ["#571845"
   "rgb(144,12,62)"
   "rgba(199,0,57,1.0)"
   "hsl(11,100%,60%)"
   "hsla(46, 97%, 48%, 1.000)"])


;; Keep in mind when writing your own `:render-fn` that it will run
;; entirely in the browser, and so will not have access to your local
;; bindings on the JVM side. If you need to your viewer to pre-process
;; what it sends to the browser, you can specify a `:transform-fn`
;; that will be called before the data is sent over the wire.

#_ "TODO example of using a :transform-fn"


;; #### 🏞 Customizing Data Fetching

;; Sometimes you might want to create a custom viewer that overrides
;; Clerk's automatic paging behavior. In this example, we use a custom
;; `fetch-fn` that specifies a `content-type` to tell Clerk to serve
;; arbitrary byte arrays as PNG images.

;; Notice that the image is conveyed out-of-band using the `url-for`
;; function to get a URL from which to fetch the blob.

;; TODO: this seems to hang the process, removed.
#_#_
(clerk/set-viewers! [{:pred bytes?
                      :fetch-fn (fn [_ bytes] {:nextjournal/content-type "image/png"
                                              :nextjournal/value bytes})
                      :render-fn (fn [blob] (v/html [:img {:src (v/url-for blob)}]))}])

(.. (HttpClient/newHttpClient)
    (send (.build (HttpRequest/newBuilder (URI. "https://upload.wikimedia.org/wikipedia/commons/5/57/James_Clerk_Maxwell.png")))
          (HttpResponse$BodyHandlers/ofByteArray)) body)


#_ "TODO need to bump Clerk version for this to work?"


;; This is just a taste of what's possible using Clerk. Take a look in
;; the `notebooks` directory to see a collection of worked examples in
;; different domains.

;; And don't forget to let us know how it goes!
