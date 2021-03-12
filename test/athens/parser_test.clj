(ns athens.parser-test
  (:require
    [athens.parser :refer [parse-to-ast parse-to-ast-new combine-adjacent-strings]]
    [clojure.test :refer [deftest is are testing]]))


(deftest parser-general-tests
  (are [x y] (= x (parse-to-ast y))
    [:block]
    ""

    [:block "OK? Yes."]
    "OK? Yes."

    [:block [:page-link "link"]]
    "[[link]]"

    [:block "A " [:page-link "link"] "."]
    "A [[link]]."

    [:block "A " [:page-link "link"] " and another " [:page-link "link"] "."]
    "A [[link]] and another [[link]]."

    [:block "Some " [:page-link "Nested " [:page-link "Links"]] " and something"]
    "Some [[Nested [[Links]]]] and something"

    [:block "[[text"]
    "[[text"

    [:block [:block-ref "V8_jUYc-k"]]
    "((V8_jUYc-k))"

    [:block "it’s " [:bold "very"] " important"]
    "it’s **very** important"))


(deftest parser-pre-formatted-tests
  (are [x y] (= x (parse-to-ast y))
    [:block "Hello " [:pre-formatted "world"]]
    "Hello `world`"

    ;; NOTE: broken in old parser
    ;; [:block "Hello " [:pre-formatted "Mars"]]
    ;; "Hello ```Mars```"

    [:block "Hello " [:pre-formatted "world"] " and " [:pre-formatted "Mars"]]
    "Hello `world` and `Mars`"

    ;; no mode detection
    ;; NOTE: broken in old parser
    ;; [:block [:pre-formatted "code here"]]
    ;; "```\ncode here\n```"

    ;; mode detection
    ;; NOTE: broken in old parser
    ;; [:block [:pre-formatted "(ns example)" "clojure"]]
    ;; "```clojure\n(ns example)```"
    ))


(deftest parser-hashtag-tests
  (are [x y] (= x (parse-to-ast y))
    [:block "some " [:hashtag "me"] " time"]
    "some #me time"

    [:block "that’s " [:hashtag "very cool"] ", yeah"]
    "that’s #[[very cool]], yeah"

    [:block "also here's " [:hashtag "nested " [:page-link "links"]] " in hashtags!"]
    "also here's #[[nested [[links]]]] in hashtags!"

    [:block "Ends after " [:hashtag "words_are_over"] "!"]
    "Ends after #words_are_over!"

    [:block "learn " [:hashtag "官话"] "?"]
    "learn #官话?"

    [:block "learn " [:hashtag "اَلْعَرَبِيَّةُ"] " in a year"]
    "learn #اَلْعَرَبِيَّةُ in a year"))


(deftest parser-component-tests
  (are [x y] (= x (parse-to-ast y))
    [:block [:component "[[TODO]]" [:page-link "TODO"]] " Pick up groceries"]
    "{{[[TODO]]}} Pick up groceries"

    [:block [:component "((block-ref-id))" [:block-ref "block-ref-id"]] " amazing block"]
    "{{((block-ref-id))}} amazing block"

    [:block [:component "AnotherComponent" "AnotherComponent"] " Another Content"]
    "{{AnotherComponent}} Another Content"))


(deftest parser-url-image-tests
  ;; Few tests because this parser largely depends on `url-link`
  (are [x y] (= x (parse-to-ast y))
    [:block [:url-image {:url "https://example.com/image.png" :alt "an example image"}]]
    "![an example image](https://example.com/image.png)"))


(deftest parser-url-link-tests
  (are [x y] (= x (time (parse-to-ast y)))
    [:block [:url-link {:url "https://example.com/"} "an example"]]
    "[an example](https://example.com/)"

    [:block [:url-link {:url "https://example.com/"} [:bold "bold"] " inside"]]
    "[**bold** inside](https://example.com/)"

    [:block [:url-link {:url "https://example.com/"} "no #hashtag or [[link]] inside"]]
    "[no #hashtag or [[link]] inside](https://example.com/)"

    [:block [:url-link {:url "https://example.com/"} "escaped ](#not-a-link)"]]
    "[escaped \\](#not-a-link)](https://example.com/)"

    [:block [:url-link {:url "https://subdomain.example.com/path/page.html?query=very%20**bold**&p=5#top"} "example"]]
    "[example](https://subdomain.example.com/path/page.html?query=very%20**bold**&p=5#top)"

    [:block [:url-link {:url "https://en.wikipedia.org/wiki/(_)_(film)"} "( )"]]
    "[( )](https://en.wikipedia.org/wiki/(_)_(film))"

    [:block [:url-link {:url "https://example.com/open_paren_'('"} "escaped ("]]
    "[escaped (](https://example.com/open_paren_'\\(')"

    [:block [:url-link {:url "https://example.com/close)open(close)"} "escaped )()"]]
    "[escaped )()](https://example.com/close\\)open\\(close\\))"

    [:block [:url-link {:url "https://example.com/close)open(close)"} "combining escaping and nesting"]]
    "[combining escaping and nesting](https://example.com/close\\)open(close))"

    [:block
     "Multiple "
     [:url-link {:url "https://example.com/a"} "links"]
     " "
     [:url-link {:url "#b"} "are detected"]
     " as "
     [:url-link {:url "https://example.com/c"} "separate"]
     "."]
    "Multiple [links](https://example.com/a) [are detected](#b) as [separate](https://example.com/c)."

    [:block [:url-link {:url "https://raw-link.com"} "https://raw-link.com"]]
    "https://raw-link.com"))


(deftest combine-adjacent-strings-tests
  (are [x y] (= x (combine-adjacent-strings y))
    []
    []

    ["some text"]
    ["some" " " "text"]

    ["some text" [:link] "around a link"]
    ["some" " " "text" [:link] "around " "a link"]

    [{:something nil} "more text" [:link] "between elements" 39]
    [{:something nil} "more" " " "text" [:link] "between" " " "elements" 39]

    [{:a 1 :b 2} 3 ["leave" "intact"]]
    [{:a 1 :b 2} 3 ["leave" "intact"]]))


(deftest parse-latex-tests
  (testing "that LaTeX syntax is detected"
    (are [x y] (= x (parse-to-ast y))
      [:block [:latex "text"]]
      "$$text$$"

      [:block [:latex "text with space"]]
      "$$text with space$$"))

  (testing "that other syntax is escaped when in LaTeX"
    (are [x y] (= x (parse-to-ast y))
      [:block [:latex "[[  ]]"]]
      "$$[[  ]]$$"

      [:block [:latex "[an example](https://example.com/)"]]
      "$$[an example](https://example.com/)$$"))

  (testing "that LaTeX is not embedded in "
    (are [x y] (= x (parse-to-ast y))
      [:block [:url-link {:url "https://example.com/"} "an $$\textLaTeX$$ example"]]
      "[an $$\textLaTeX$$ example](https://example.com/)"))

  (testing "that LaTeX expressions can have $ in them"
    (is (= [:block [:latex "a b $ c"]]
           (parse-to-ast "$$a b $ c$$")))))


(deftest parser-new-general-tests
  (are [x y] (= x (parse-to-ast-new y))
    [:block]
    ""

    [:block "OK? Yes."]
    "OK? Yes."

    [:block [:page-link "link"]]
    "[[link]]"

    [:block "A " [:page-link "link"] "."]
    "A [[link]]."

    [:block "A " [:page-link "link"] " and another " [:page-link "link"] "."]
    "A [[link]] and another [[link]]."

    [:block "Some " [:page-link "Nested " [:page-link "Links"]] " and something"]
    "Some [[Nested [[Links]]]] and something"

    [:block "[[text"]
    "[[text"

    [:block [:block-ref "V8_jUYc-k"]]
    "((V8_jUYc-k))"

    [:block "it’s " [:bold "very"] " important"]
    "it’s **very** important"))


(deftest parser-new-pre-formatted-tests
  (are [x y] (= x (parse-to-ast-new y))
    [:block "Hello " [:inline-pre-formatted "world"]]
    "Hello `world`"

    [:block "Hello " [:block-pre-formatted "Mars"]]
    "Hello ```Mars```"

    [:block "Hello " [:inline-pre-formatted "world"] " and " [:inline-pre-formatted "Mars"]]
    "Hello `world` and `Mars`"

    ;; no mode detection
    [:block [:block-pre-formatted "code here"]]
    "```\ncode here\n```"

    ;; mode detection
    [:block [:block-pre-formatted "(ns example)" "clojure"]]
    "```clojure\n(ns example)```"

    ;; code blocks with backticks
    [:block [:block-pre-formatted "a`b`c"]]
    "```a`b`c```"))


(deftest parser-new-hashtag-tests
  (are [x y] (= x (parse-to-ast-new y))
    [:block "some " [:hashtag "me"] " time"]
    "some #me time"

    [:block "that’s " [:hashtag "very cool"] ", yeah"]
    "that’s #[[very cool]], yeah"

    [:block "also here's " [:hashtag "nested " [:page-link "links"]] " in hashtags!"]
    "also here's #[[nested [[links]]]] in hashtags!"

    [:block "Ends after " [:hashtag "words_are_over"] "!"]
    "Ends after #words_are_over!"

    [:block "learn " [:hashtag "官话"] "?"]
    "learn #官话?"

    [:block "learn " [:hashtag "اَلْعَرَبِيَّةُ"] " in a year"]
    "learn #اَلْعَرَبِيَّةُ in a year"))


(deftest parser-new-component-tests
  (are [x y] (= x (parse-to-ast-new y))
    [:block [:component "[[TODO]]" [:page-link "TODO"]] " Pick up groceries"]
    "{{[[TODO]]}} Pick up groceries"

    [:block [:component "((block-ref-id))" [:block-ref "block-ref-id"]] " amazing block"]
    "{{((block-ref-id))}} amazing block"

    [:block [:component "AnotherComponent" "AnotherComponent"] " Another Content"]
    "{{AnotherComponent}} Another Content"))


(deftest parser-new-url-image-tests
  ;; Few tests because this parser largely depends on `url-link`
  (are [x y] (= x (parse-to-ast-new y))
    [:block [:url-image {:url "https://example.com/image.png" :alt "an example image"}]]
    "![an example image](https://example.com/image.png)"))


(deftest parser-new-url-link-tests
  (testing "valid cases"
    (are [x y] (= x (time (parse-to-ast-new y)))
      [:block [:url-link {:url "https://example.com/"} "an example"]]
      "[an example](https://example.com/)"

      [:block [:url-link {:url "https://example.com/"} [:bold "bold"] " inside"]]
      "[**bold** inside](https://example.com/)"

      [:block [:url-link {:url "https://subdomain.example.com/path/page.html?query=very%20**bold**&p=5#top"} "example"]]
      "[example](https://subdomain.example.com/path/page.html?query=very%20**bold**&p=5#top)"

      ;; raw-url with parens
      [:block [:url-link {:url "https://en.wikipedia.org/wiki/(_)_(film)"}
               "https://en.wikipedia.org/wiki/(_)_(film)"]]
      "https://en.wikipedia.org/wiki/(_)_(film)"

      [:block [:url-link {:url "https://en.wikipedia.org/wiki/(_)_(film)"} "( )"]]
      "[( )](https://en.wikipedia.org/wiki/(_)_(film))"

      ;; raw-url with parens escaped
      [:block [:url-link {:url "https://example.com/open_paren_'\\('"}
               "https://example.com/open_paren_'\\('"]]
      "https://example.com/open_paren_'\\('"

      [:block [:url-link {:url "https://example.com/open_paren_'\\('"} "escaped ("]]
      "[escaped (](https://example.com/open_paren_'\\(')"

      ;; raw-url with more escaped parens
      [:block [:url-link {:url "https://example.com/close\\)open\\(close\\)"}
               "https://example.com/close\\)open\\(close\\)"]]
      "https://example.com/close\\)open\\(close\\)"

      [:block [:url-link {:url "https://example.com/close\\)open\\(close\\)"} "escaped )()"]]
      "[escaped )()](https://example.com/close\\)open\\(close\\))"

      [:block [:url-link {:url "https://example.com/close\\)open(close)"}
               "https://example.com/close\\)open(close)"]]
      "https://example.com/close\\)open(close)"

      [:block [:url-link {:url "https://example.com/close\\)open(close)"} "combining escaping and nesting"]]
      "[combining escaping and nesting](https://example.com/close\\)open(close))"

      [:block
       "Multiple "
       [:url-link {:url "https://example.com/a"} "links"]
       " "
       [:url-link {:url "#b"} "are detected"]
       " as "
       [:url-link {:url "https://example.com/c"} "separate"]
       "."]
      "Multiple [links](https://example.com/a) [are detected](#b) as [separate](https://example.com/c)."

      [:block [:url-link {:url "https://raw-link.com"} "https://raw-link.com"]]
      "https://raw-link.com"))

  (testing "invalid cases"
    (are [x] (contains? (meta (parse-to-ast-new x)) :parse-error)
      ;; TODO: it's probably better to return input string with parse error data
      ;; [:block [:url-link {:url "https://example.com/"} "no #hashtag or [[link]] inside"]]
      ;; "[no #hashtag or [[link]] inside](https://example.com/)"
      "[no #hashtag or [[link]] inside](https://example.com/)"

      ;; TODO: do we really want to support escaped `]` inside of `[]` block
      "[escaped \\](#not-a-link)](https://example.com/)")))
