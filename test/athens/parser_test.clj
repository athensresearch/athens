(ns athens.parser-test
  (:require
    [athens.parser :refer [parse-to-ast combine-adjacent-strings]]
    [clojure.test :refer [deftest is are]]))


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

    [:block "Hello " [:pre-formatted "Mars"]]
    "Hello ```Mars```"

    [:block "Hello " [:pre-formatted "world"] " and " [:pre-formatted "Mars"]]
    "Hello `world` and `Mars`"))


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

    [:block [:component "AnotherComponent" "AnotherComponent"] " Another Content"]
    "{{AnotherComponent}} Another Content"))


(deftest parser-url-image-tests
  ;; Few tests because this parser largely depends on `url-link`
  (are [x y] (= x (parse-to-ast y))
    [:block [:url-image {:url "https://example.com/image.png" :alt "an example image"}]]
    "![an example image](https://example.com/image.png)"))


(deftest parser-url-link-tests
  (are [x y] (= x (parse-to-ast y))
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
    "Multiple [links](https://example.com/a) [are detected](#b) as [separate](https://example.com/c)."))


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
