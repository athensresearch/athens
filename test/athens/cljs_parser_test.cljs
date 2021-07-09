(ns athens.cljs-parser-test
  "Testing parsing in CLJS, since our parser can behave differently in CLJ & CLJS."
  (:require
    [athens.parser.impl :as sut]
    [clojure.test :as t])
  (:require-macros
    [athens.cljs-parser-test :as util]))


(t/deftest block-structure

  (t/testing "that headings are parsed"

    (util/parses-to sut/block-parser->ast

                    "# Heading"
                    [:block [:heading {:n    1
                                       :from "# Heading"}
                             [:paragraph-text "Heading"]]]

                    "# Heading\n\n"
                    [:block [:heading {:n    1
                                       :from "# Heading"}
                             [:paragraph-text "Heading"]]]

                    "### Heading\n"
                    [:block [:heading {:n    3
                                       :from "### Heading"}
                             [:paragraph-text "Heading"]]]))

  (t/testing "that thematic-breaks are parsed"

    (util/parses-to sut/block-parser->ast

                    "***"
                    [:block
                     [:paragraph-text "***"]]

                    "***bold and italic***"
                    [:block
                     [:paragraph-text "***bold and italic***"]]

                    "---"
                    [:block [:thematic-break "---"]]

                    "___"
                    [:block [:thematic-break "___"]]))

  (t/testing "that indented-code-blocks are parsed"

    (util/parses-to sut/block-parser->ast

                    "    some code"
                    [:block [:indented-code-block {:from "    some code"}
                             [:code-text "some code"]]]

                    "    multiline\n    code"
                    [:block [:indented-code-block {:from "    multiline\n    code"}
                             [:code-text "multiline\ncode"]]]

                    "    multiline\n    code\n      with indentation"
                    [:block [:indented-code-block {:from "    multiline\n    code\n      with indentation"}
                             [:code-text "multiline\ncode\n  with indentation"]]]))

  (t/testing "that fenced-code-blocks are parsed"

    (util/parses-to sut/block-parser->ast

                    "```\nsome code```"
                    [:block [:fenced-code-block {:lang ""}
                             [:code-text "some code"]]]

                    "```javascript\nvar a = 1;\n```"
                    [:block [:fenced-code-block {:lang "javascript"}
                             [:code-text "var a = 1;"]]]

                    "```javascript\nvar a = \"with` ticks`\";\nand multiline```"
                    [:block [:fenced-code-block {:lang "javascript"}
                             [:code-text "var a = \"with` ticks`\";\nand multiline"]]]))

  (t/testing "that paragraphs are parsed"

    (util/parses-to sut/block-parser->ast

                    "aaa"
                    [:block [:paragraph-text "aaa"]]

                    "aaa\n\nbbb"
                    [:block
                     [:paragraph-text "aaa"]
                     [:paragraph-text "bbb"]]

                    "aaa\nbbb\n\nccc\nddd"
                    [:block
                     [:paragraph-text "aaa\nbbb"]
                     [:paragraph-text "ccc\nddd"]]

                    "aaa\n\n\nbbb"
                    [:block
                     [:paragraph-text "aaa"]
                     [:paragraph-text "bbb"]]

                    "  aaa\n bbb" ; leading spaces are skipped
                    [:block [:paragraph-text "aaa\nbbb"]]

                    "aaa\n    bbb\n        ccc"
                    [:block [:paragraph-text "aaa\nbbb\nccc"]]

                    "   aaa\nbbb" ; 3 spaces max
                    [:block [:paragraph-text "aaa\nbbb"]]

                    "    aaa\nbbb" ; or code block is triggered
                    [:block
                     [:indented-code-block {:from "    aaa"}
                      [:code-text "aaa"]]
                     [:paragraph-text "bbb"]]))

  (t/testing "that block-quote is parsed"

    (util/parses-to sut/block-parser->ast

                    "> # Foo
> bar
> baz"
                    [:block [:block-quote
                             [:heading {:n    1
                                        :from "# Foo"}
                              [:paragraph-text "Foo"]]
                             [:paragraph-text "bar\nbaz"]]]

                    ;; spaces after `>` can be omitted
                    "># Foo
>bar
> baz"
                    [:block [:block-quote
                             [:heading {:n    1
                                        :from "# Foo"}
                              [:paragraph-text "Foo"]]
                             [:paragraph-text "bar\nbaz"]]]

                    ;; The > characters can be indented 1-3 spaces
                    "   > # Foo
   > bar
 > baz"
                    [:block [:block-quote
                             [:heading {:n    1
                                        :from "# Foo"}
                              [:paragraph-text "Foo"]]
                             [:paragraph-text "bar\nbaz"]]]

                    ;; Four spaces gives us a code block:
                    "    > # Foo
    > bar
    > baz"
                    [:block [:indented-code-block {:from "    > # Foo
    > bar
    > baz"}
                             [:code-text "> # Foo\n> bar\n> baz"]]]

                    ;; block quote is a container for other blocks
                    "> aaa
> 
> bbb"
                    [:block [:block-quote
                             [:paragraph-text "aaa"]
                             [:paragraph-text "bbb"]]]

                    ;; nested block quotes
                    "> > aaa
> > bbb
> > ccc"
                    [:block
                     [:block-quote
                      [:block-quote
                       [:paragraph-text "aaa\nbbb\nccc"]]]]

                    ">> aa\n>> bb"
                    [:block
                     [:block-quote
                      [:block-quote
                       [:paragraph-text "aa\nbb"]]]]

                    ">     code

>    not code"
                    [:block
                     [:block-quote [:indented-code-block {:from "    code"}
                                    [:code-text "code"]]]
                     [:block-quote [:paragraph-text "not code"]]]

                    "> ```code\n> more```

>    not code"
                    [:block
                     [:block-quote
                      [:fenced-code-block {:lang "code"} [:code-text "more"]]]
                     [:block-quote [:paragraph-text "not code"]]])))


(t/deftest inline-structure

  (t/testing "backslash escapes"
    (util/parses-to sut/inline-parser->ast

                    ;; Any ASCII punctuation character may be backslash-escaped
                    "\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\\\\\]\\^\\_\\`\\{\\|\\}\\~"
                    [:paragraph
                     [:text-run
                      "\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\=\\>\\?\\@\\[\\\\\\]\\^\\_\\`\\{\\|\\}\\~"]]

                    ;; Backslashes before other characters are treated as literal backslashes:
                    "\\→\\A\\a\\ \\3\\φ\\«"
                    [:paragraph
                     [:text-run "\\→\\A\\a\\ \\3\\φ\\«"]]))

  (t/testing "code spans"
    (util/parses-to sut/inline-parser->ast

                    ;; code spans
                    "`abc`"
                    [:paragraph
                     [:code-span "abc"]]

                    "`foo ` bar`"
                    [:paragraph
                     [:code-span "foo ` bar"]]))

  (t/testing "all sorts of emphasis"
    (util/parses-to sut/inline-parser->ast

                    ;; emphasis & strong emphasis
                    "*emphasis*"
                    [:paragraph
                     [:emphasis
                      [:text-run "emphasis"]]]

                    "* not em *"
                    [:paragraph
                     [:text-run "* not em *"]]

                    "**strong**"
                    [:paragraph
                     [:strong-emphasis
                      [:text-run "strong"]]]

                    ;; mix and match different emphasis
                    "**bold and *italic***"
                    [:paragraph
                     [:strong-emphasis
                      [:text-run "bold and "]
                      [:emphasis
                       [:text-run "italic"]]]]

                    "***bold and italic***"
                    [:paragraph
                     [:strong-emphasis
                      [:emphasis
                       [:text-run "bold and italic"]]]]

                    ;; next to each other
                    "normal *italic* **bold**"
                    [:paragraph
                     [:text-run "normal "]
                     [:emphasis [:text-run "italic"]]
                     [:text-run " "]
                     [:strong-emphasis [:text-run "bold"]]]))

  (t/testing "highlights (local Athens extension `^^...^^`)"
    (util/parses-to sut/inline-parser->ast

                    ;; just a highlight
                    "^^NEW^^"
                    [:paragraph
                     [:highlight
                      [:text-run "NEW"]]]

                    ;; in a middle
                    "something ^^completely^^ different"
                    [:paragraph
                     [:text-run "something "]
                     [:highlight [:text-run "completely"]]
                     [:text-run " different"]]

                    ;; with spaces
                    "^^a b c^^"
                    [:paragraph
                     [:highlight
                      [:text-run "a b c"]]]

                    ;; mixing with emphasis
                    "this ^^highlight *has* **emphasis**^^"
                    [:paragraph
                     [:text-run "this "]
                     [:highlight
                      [:text-run "highlight "]
                      [:emphasis [:text-run "has"]]
                      [:text-run " "]
                      [:strong-emphasis [:text-run "emphasis"]]]]

                    "this ^^highlight **has *nested emphasis***^^"
                    [:paragraph
                     [:text-run "this "]
                     [:highlight
                      [:text-run "highlight "]
                      [:strong-emphasis
                       [:text-run "has "]
                       [:emphasis [:text-run "nested emphasis"]]]]]))

  (t/testing "strikethrough (GFM extension)"
    (util/parses-to sut/inline-parser->ast

                    "~~Hi~~ Hello, world!"
                    [:paragraph
                     [:strikethrough [:text-run "Hi"]]
                     [:text-run " Hello, world!"]]

                    ;; not in the middle of the word
                    "T~~hi~~s"
                    [:paragraph
                     [:text-run "T~~hi~~s"]]

                    ;; no spaces inside
                    "Ain't ~~ working ~~"
                    [:paragraph
                     [:text-run "Ain't ~~ working ~~"]]))

  (t/testing "links"
    (util/parses-to sut/inline-parser->ast

                    "[link text](/some/url)"
                    [:paragraph
                     [:link {:text   "link text"
                             :target "/some/url"}]]

                    ;; 3 sorts of link title
                    "[link text](/some/url \"title\")"
                    [:paragraph
                     [:link {:text   "link text"
                             :target "/some/url"
                             :title  "title"}]]

                    "[link text](/some/url 'title')"
                    [:paragraph
                     [:link {:text   "link text"
                             :target "/some/url"
                             :title  "title"}]]

                    "[link text](/some/url (title))"
                    [:paragraph
                     [:link {:text   "link text"
                             :target "/some/url"
                             :title  "title"}]]

                    ;; link in an emphasis
                    "this **[link](/example) is bold**"
                    [:paragraph
                     [:text-run "this "]
                     [:strong-emphasis
                      [:link {:text   "link"
                              :target "/example"}]
                      [:text-run " is bold"]]]

                    ;; but no emphasis in a link
                    "[*em*](/link)"
                    [:paragraph
                     [:link {:text   "*em*"
                             :target "/link"}]]

                    ;; because of fs usage targets can have spaces
                    "[b c d](/url/with space)"
                    [:paragraph
                     [:link {:text   "b c d"
                             :target "/url/with space"}]]

                    "[b c d](/url/with space (and title))"
                    [:paragraph
                     [:link {:text   "b c d"
                             :target "/url/with space"
                             :title  "and title"}]]))

  (t/testing "images"
    (util/parses-to sut/inline-parser->ast

                    "![link text](/some/url)"
                    [:paragraph
                     [:url-image {:alt "link text"
                                  :src "/some/url"}]]

                    ;; 3 sorts of link title
                    "![link text](/some/url \"title\")"
                    [:paragraph
                     [:url-image {:alt   "link text"
                                  :src   "/some/url"
                                  :title "title"}]]

                    "![link text](/some/url 'title')"
                    [:paragraph
                     [:url-image {:alt   "link text"
                                  :src   "/some/url"
                                  :title "title"}]]

                    "![link text](/some/url (title))"
                    [:paragraph
                     [:url-image {:alt   "link text"
                                  :src   "/some/url"
                                  :title "title"}]]

                    ;; link in an emphasis
                    "this **![link](/example) is bold**"
                    [:paragraph
                     [:text-run "this "]
                     [:strong-emphasis
                      [:url-image {:alt "link"
                                   :src "/example"}]
                      [:text-run " is bold"]]]

                    ;; but no emphasis in a link
                    "![*em*](/link)"
                    [:paragraph
                     [:url-image {:alt "*em*"
                                  :src "/link"}]]

                    ;; image link with spaces
                    "![image alt text](/url/with spaces)"
                    [:paragraph
                     [:url-image {:alt "image alt text"
                                  :src "/url/with spaces"}]]

                    "![image alt text](/url with spaces \"and title\")"
                    [:paragraph
                     [:url-image {:alt   "image alt text"
                                  :src   "/url with spaces"
                                  :title "and title"}]]))

  (t/testing "autolinks"
    (util/parses-to sut/inline-parser->ast

                    "<http://example.com>"
                    [:paragraph
                     [:autolink {:text   "http://example.com"
                                 :target "http://example.com"}]]

                    ;; no white space in autolinks
                    "<http://example.com and>"
                    [:paragraph
                     [:text-run "<http://example.com and>"]]

                    ;; emails are recognized
                    "<root@example.com>"
                    [:paragraph
                     [:autolink {:text   "root@example.com"
                                 :target "mailto:root@example.com"}]]

                    ;; multiple auto links
                    "<first> and <second>"
                    [:paragraph
                     [:autolink {:text   "first"
                                 :target "first"}]
                     [:text-run " and "]
                     [:autolink {:text   "second"
                                 :target "second"}]]))

  (t/testing "block references (Athens extension)"
    (util/parses-to sut/inline-parser->ast

                    ;; just a block-ref
                    "((block-id))"
                    [:paragraph
                     [:block-ref {:from "((block-id))"}
                      "block-id"]]

                    ;; in a middle of text-run
                    "Text with ((block-id)) a block"
                    [:paragraph
                     [:text-run "Text with "]
                     [:block-ref {:from "((block-id))"}
                      "block-id"]
                     [:text-run " a block"]]

                    "And ((block-id1)) multiple ((block-id2)) times"
                    [:paragraph
                     [:text-run "And "]
                     [:block-ref {:from "((block-id1))"}
                      "block-id1"]
                     [:text-run " multiple "]
                     [:block-ref {:from "((block-id2))"}
                      "block-id2"]
                     [:text-run " times"]]

                    ;; block refs can appear in words
                    "a((block-id))b"
                    [:paragraph
                     [:text-run "a"]
                     [:block-ref {:from "((block-id))"}
                      "block-id"]
                     [:text-run "b"]]))

  (t/testing "hard line breaks"
    (util/parses-to sut/inline-parser->ast

                    ;; hard line break can be only at the end of a line
                    "abc  \ndef"
                    [:paragraph
                     [:text-run "abc  "]
                     [:hard-line-break]
                     [:text-run "def"]]

                    "abc  \n\ndef"
                    [:paragraph
                     [:text-run "abc  "]
                     [:hard-line-break]
                     [:newline "\n"]
                     [:text-run "def"]]

                    "abc  \n\n\ndef"
                    [:paragraph
                     [:text-run "abc  "]
                     [:hard-line-break]
                     [:newline "\n"]
                     [:hard-line-break]
                     [:text-run "def"]]))

  (t/testing "page links (Athens extension)"
    (util/parses-to sut/inline-parser->ast
                    "[[Page Title]]"
                    [:paragraph
                     [:page-link {:from "[[Page Title]]"}
                      "Page Title"]]

                    "In a middle [[Page Title]] of text"
                    [:paragraph
                     [:text-run "In a middle "]
                     [:page-link {:from "[[Page Title]]"}
                      "Page Title"]
                     [:text-run " of text"]]

                    ;; But not when surrounded by word
                    "abc[[def]]ghi"
                    [:paragraph
                     [:text-run "abc[[def]]ghi"]]

                    ;; also can't span newline
                    "abc [[def\nghil]] jkl"
                    [:paragraph
                     [:text-run "abc [[def"]
                     [:newline "\n"]
                     [:text-run "ghil]] jkl"]]

                    ;; apparently nesting page links is a thing
                    "[[nesting [[nested]]]]"
                    [:paragraph
                     [:page-link {:from "[[nesting [[nested]]]]"}
                      "nesting "
                      [:page-link {:from "[[nested]]"}
                       "nested"]]]

                    ;; Multiple page links in one blok
                    "[[one]] and [[two]]"
                    [:paragraph
                     [:page-link {:from "[[one]]"}
                      "one"]
                     [:text-run " and "]
                     [:page-link {:from "[[two]]"}
                      "two"]]))

  (t/testing "hashtags (Athens extension)"
    (util/parses-to sut/inline-parser->ast
                    "#[[Page Title]]"
                    [:paragraph
                     [:hashtag {:from "#[[Page Title]]"}
                      "Page Title"]]

                    "In a middle #[[Page Title]] of text"
                    [:paragraph
                     [:text-run "In a middle "]
                     [:hashtag {:from "#[[Page Title]]"}
                      "Page Title"]
                     [:text-run " of text"]]

                    ;; But not when surrounded by word
                    "abc#[[def]]ghi"
                    [:paragraph
                     [:text-run "abc#[[def]]ghi"]]

                    ;; also can't span newline
                    "abc #[[def\nghil]] jkl"
                    [:paragraph
                     [:text-run "abc #[[def"]
                     [:newline "\n"]
                     [:text-run "ghil]] jkl"]]

                    ;; hashtags can also be without `[[]]`
                    "#simple"
                    [:paragraph
                     [:hashtag {:from "#simple"}
                      "simple"]]

                    ;; can be in a middle of a text run
                    "abc #simple def"
                    [:paragraph
                     [:text-run "abc "]
                     [:hashtag {:from "#simple"}
                      "simple"]
                     [:text-run " def"]]

                    ;; but not in a word run
                    "abc#not-hashtag"
                    [:paragraph
                     [:text-run "abc#not-hashtag"]]))

  (t/testing "components (Athens extension)"
    (util/parses-to sut/inline-parser->ast

                    ;; plain text component
                    "{{component}}"
                    [:paragraph
                     [:component "component" "component"]]

                    ;; page link component
                    "{{[[DONE]]}} components"
                    [:paragraph
                     [:component "[[DONE]]" [:page-link {:from "[[DONE]]"} "DONE"]]
                     [:text-run " components"]]

                    ;; block ref in component
                    "{{((abc))}}"
                    [:paragraph
                     [:component "((abc))" [:block-ref {:from "((abc))"} "abc"]]]))

  (t/testing "LaTeX (Athens extension)"
    (util/parses-to sut/inline-parser->ast

                    "$$\\LaTeX$$"
                    [:paragraph
                     [:latex "\\LaTeX"]]

                    ;; can have newlines inside
                    ;; NOTE: not working in JS environment same as in JVM
                    "$$abc\ndef$$"
                    [:paragraph
                     [:text-run "$$abc"]
                     [:newline "\n"]
                     [:text-run "def$$"]]

                    ;; can have $ inside
                    "$$abc $ d$$"
                    [:paragraph
                     [:latex "abc $ d"]]

                    ;; can't have $$
                    "$$abc $$ def$$"
                    [:paragraph
                     [:latex "abc "]
                     [:text-run " def$$"]]

                    ;; Multiple LaTeX fragments in one block
                    "$$G, \\mu$$ and Poisson's ratio $$\\nu$$"
                    [:paragraph
                     [:latex "G, \\mu"]
                     [:text-run " and Poisson's ratio "]
                     [:latex "\\nu"]])))


(t/deftest staged-parser-tests

  (t/testing "Some random MD contents"
    (util/parses-to sut/staged-parser->ast
                    "# Defluxere caelesti omnia

## Vixque acrior praedelassat vixque iussit quam speciem

Lorem [markdownum deserto](http://est.com/mihicessasse) tamen, puellae annis
quaesitae medio ego, et felix, ingestoque ante, Chariclo torum. Epaphi quod qui
maternaque concava nunc artes sortita, nam isto. Corpore nitebant fero. Telo
[caesus](http://audaci-terris.com/per.html), ait aliquid non ipse *cum omine*,
lacerare gaudia mittere sermonibus. Tuta [auspicio admiremur
murmura](http://www.vires-remittit.net/alcmene-potitur.php) Troades lilia places
incubuit carinae, palustres excipit.

- Licet contendere admovit saevae ictus pervidet Tyrios
- Opacas antiqua capitis corpore silentia portasque haec
- Quoque tertius avidamque victorem iners
- Umbra sinuosa femina agitavit regia
- Ventisque sortibus

## Agam sed tantum levavit nimiumque bellum recondidit

Praeconsumere illuc et dixi iubet risisse: colunt *Iuno* auribus clara: loca
utero sine prolisque in sui et in. Pelagi Aurora Actaeon, silva plenissima
omnia, armentaque et quid ponto, tu. Odium litoream Iulius et sorte mutatus
instabilis prohibete nunc pestifera? Iunone vos vident, ovis gratissime misceri
et adiuvet conplevit Chromin coniunx congeriem.

Non sequitur tenuique. Hinc Miletum hospitio adeo omnia medius Theseus locus
menti meminisse Phoebe meumque **armenta**. Quae undas morsa seu iubas
dimittere? Ab se certaminis exitus lacertis [obsisto
dicenda](http://postquamaeratae.io/ferar.html) sagitta iugulum.

## Non flamma hic armorum dulces nec purpureas

Mea suas vos Troiae non claro satis, illa non. Spectatrix habes; nec has
Emathides cantatas, submovit puer pumice ipse, proles innumeris an parem et
quam.

## Quos superat voluptas

Aquas in coniuge cornua. Quem dixit Nelei, tibi preces inplicat undis, seu nisi
nubes, in terrae [contenta mihi tum](http://www.monstravit.org/) fatus tectis.

> Neptis albenti urbes aether nostro pigeat frons: iacet latis vobis; potest
> facta Charopem et oscula. Mihi sunt fateri; heu plenum ova!

Nondum supero in vocavit adspicit nec sine prodidit. Insula fugit alterno
praeterea cadentem [iacebas Lucifer
nostris](http://oconcipit.io/tamen-vestibus). Et et quandoquidem pavens, fiat
specie Achivi suus publica Marte extimuit. Ferro domos suras."
                    [:block
                     [:heading {:n    1
                                :from "# Defluxere caelesti omnia"}
                      [:paragraph "Defluxere caelesti omnia"]]
                     [:heading {:n    2
                                :from "## Vixque acrior praedelassat vixque iussit quam speciem"}
                      [:paragraph "Vixque acrior praedelassat vixque iussit quam speciem"]]
                     [:paragraph
                      "Lorem "
                      [:link
                       {:text "markdownum deserto", :target "http://est.com/mihicessasse"}]
                      " tamen, puellae annis"
                      [:newline "\n"]
                      "quaesitae medio ego, et felix, ingestoque ante, Chariclo torum. Epaphi quod qui"
                      [:newline "\n"]
                      "maternaque concava nunc artes sortita, nam isto. Corpore nitebant fero. Telo"
                      [:newline "\n"]
                      [:link {:text "caesus", :target "http://audaci-terris.com/per.html"}]
                      ", ait aliquid non ipse "
                      [:italic "cum omine"]
                      ","
                      [:newline "\n"]
                      "lacerare gaudia mittere sermonibus. Tuta "
                      [:link
                       {:text   "auspicio admiremur\nmurmura",
                        :target "http://www.vires-remittit.net/alcmene-potitur.php"}]
                      " Troades lilia places"
                      [:newline "\n"]
                      "incubuit carinae, palustres excipit."]
                     [:paragraph
                      "- Licet contendere admovit saevae ictus pervidet Tyrios"
                      [:newline "\n"]
                      "- Opacas antiqua capitis corpore silentia portasque haec"
                      [:newline "\n"]
                      "- Quoque tertius avidamque victorem iners"
                      [:newline "\n"]
                      "- Umbra sinuosa femina agitavit regia"
                      [:newline "\n"]
                      "- Ventisque sortibus"]
                     [:heading {:n    2
                                :from "## Agam sed tantum levavit nimiumque bellum recondidit"}
                      [:paragraph "Agam sed tantum levavit nimiumque bellum recondidit"]]
                     [:paragraph
                      "Praeconsumere illuc et dixi iubet risisse: colunt "
                      [:italic "Iuno"]
                      " auribus clara: loca"
                      [:newline "\n"]
                      "utero sine prolisque in sui et in. Pelagi Aurora Actaeon, silva plenissima"
                      [:newline "\n"]
                      "omnia, armentaque et quid ponto, tu. Odium litoream Iulius et sorte mutatus"
                      [:newline "\n"]
                      "instabilis prohibete nunc pestifera? Iunone vos vident, ovis gratissime misceri"
                      [:newline "\n"]
                      "et adiuvet conplevit Chromin coniunx congeriem."]
                     [:paragraph
                      "Non sequitur tenuique. Hinc Miletum hospitio adeo omnia medius Theseus locus"
                      [:newline "\n"]
                      "menti meminisse Phoebe meumque "
                      [:bold "armenta"]
                      ". Quae undas morsa seu iubas"
                      [:newline "\n"]
                      "dimittere? Ab se certaminis exitus lacertis "
                      [:link
                       {:text   "obsisto\ndicenda",
                        :target "http://postquamaeratae.io/ferar.html"}]
                      " sagitta iugulum."]
                     [:heading {:n    2
                                :from "## Non flamma hic armorum dulces nec purpureas"}
                      [:paragraph "Non flamma hic armorum dulces nec purpureas"]]
                     [:paragraph
                      "Mea suas vos Troiae non claro satis, illa non. Spectatrix habes; nec has"
                      [:newline "\n"]
                      "Emathides cantatas, submovit puer pumice ipse, proles innumeris an parem et"
                      [:newline "\n"]
                      "quam."]
                     [:heading {:n    2
                                :from "## Quos superat voluptas"}
                      [:paragraph "Quos superat voluptas"]]
                     [:paragraph
                      "Aquas in coniuge cornua. Quem dixit Nelei, tibi preces inplicat undis, seu nisi"
                      [:newline "\n"]
                      "nubes, in terrae "
                      [:link
                       {:text "contenta mihi tum", :target "http://www.monstravit.org/"}]
                      " fatus tectis."]
                     [:blockquote
                      [:paragraph
                       "Neptis albenti urbes aether nostro pigeat frons: iacet latis vobis; potest"
                       [:newline "\n"]
                       "facta Charopem et oscula. Mihi sunt fateri; heu plenum ova!"]]
                     [:paragraph
                      "Nondum supero in vocavit adspicit nec sine prodidit. Insula fugit alterno"
                      [:newline "\n"]
                      "praeterea cadentem "
                      [:link
                       {:text   "iacebas Lucifer\nnostris",
                        :target "http://oconcipit.io/tamen-vestibus"}]
                      ". Et et quandoquidem pavens, fiat"
                      [:newline "\n"]
                      "specie Achivi suus publica Marte extimuit. Ferro domos suras."]])))
