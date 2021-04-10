(ns athens.parser.compatibility-test
  (:require
    [athens.parser.impl :as sut]
    [clojure.test :as t :refer [deftest is are testing]]))


(deftest parser-general-tests
  (are [x y] (= x (sut/staged-parser->ast y))
    [:block]
    ""

    [:block [:paragraph "OK? Yes."]]
    "OK? Yes."

    [:block [:paragraph [:page-link "link"]]]
    "[[link]]"

    [:block [:paragraph "A " [:page-link "link"] "."]]
    "A [[link]]."

    [:block [:paragraph "A " [:page-link "link"] " and another " [:page-link "link"] "."]]
    "A [[link]] and another [[link]]."

    [:block [:paragraph "Some " [:page-link "Nested " [:page-link "Links"]] " and something"]]
    "Some [[Nested [[Links]]]] and something"

    [:block [:paragraph "[" "[" "text"]]
    "[[text"

    [:block [:paragraph [:block-ref "V8_jUYc-k"]]]
    "((V8_jUYc-k))"

    [:block [:paragraph "it’s " [:bold "very"] " important"]]
    "it’s **very** important"))


(deftest parser-pre-formatted-tests
  (are [x y] (= x (sut/staged-parser->ast y))
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
  (are [x y] (= x (sut/staged-parser->ast y))
    [:block [:paragraph "some " [:hashtag "me"] " time"]]
    "some #me time"

    [:block [:paragraph "that’s " [:hashtag "very cool"] ", yeah"]]
    "that’s #[[very cool]], yeah"

    [:block [:paragraph "also here's " [:hashtag "nested " [:page-link "links"]] " in hashtags" "!"]]
    "also here's #[[nested [[links]]]] in hashtags!"

    [:block [:paragraph "Ends after " [:hashtag "words_are_over"] "!"]]
    "Ends after #words_are_over!"

    [:block [:paragraph "learn " [:hashtag "官话"] "?"]]
    "learn #官话?"

    [:block [:paragraph "learn " [:hashtag "اَلْعَرَبِيَّةُ"] " in a year"]]
    "learn #اَلْعَرَبِيَّةُ in a year"))


(deftest parser-component-tests
  (are [x y] (= x (sut/staged-parser->ast y))
    [:block [:paragraph [:component "[[TODO]]" [:page-link "TODO"]] " Pick up groceries"]]
    "{{[[TODO]]}} Pick up groceries"

    [:block [:paragraph [:component "((block-ref-id))" [:block-ref "block-ref-id"]] " amazing block"]]
    "{{((block-ref-id))}} amazing block"

    [:block [:paragraph [:component "AnotherComponent" "AnotherComponent"] " Another Content"]]
    "{{AnotherComponent}} Another Content"))


(deftest parser-url-image-tests
  ;; Few tests because this parser largely depends on `url-link`
  (are [x y] (= x (sut/staged-parser->ast y))
    [:block [:url-image {:url "https://example.com/image.png" :alt "an example image"}]]
    "![an example image](https://example.com/image.png)"))


(deftest parser-raw-url-tests
  (are [x y] (= x (sut/staged-parser->ast y))
    ; Basic URLs in plain text
    [:block
     "First URL: "
     [:url-link {:url "https://example.com/1"} "https://example.com/1"]
     " second URL: "
     [:url-link {:url "https://example.com/2"} "https://example.com/2"]]
    "First URL: https://example.com/1 second URL: https://example.com/2"

    ; URL following a TODO component
    [:block [:component "[[TODO]]"
             [:page-link "TODO"]]
     " read: " [:url-link {:url "https://www.example.com"} "https://www.example.com"]]
    "{{[[TODO]]}} read: https://www.example.com"

    ; URL with fragment following a TODO component
    [:block [:component "[[TODO]]"
             [:page-link "TODO"]]
     " " [:url-link {:url "https://example.com#fragment"} "https://example.com#fragment"]]
    "{{[[TODO]]}} https://example.com#fragment"))


; Test cases for blocks that only contain a single raw URL that should be parsed
; as a link. Those mostly test the URL parser.
(deftest parser-lone-raw-url-tests
  (are [url] (= [:block [:paragraph [:span [:link {:text   url
                                                   :target url}]]]] (sut/staged-parser->ast url))
    "https://example.com"
    ; URL with path set to /.
    "https://example.com/"
    ; URL with text fragment (see https://web.dev/text-fragments/) that ends with a period.
    "https://www.glassdoor.com/Interview/Would-you-rather-fight-1-horse-sized-duck-or-100-duck-sized-horses-QTN_1182586.htm#:~:text=I%20would%20rather%20fight%20100,would%20give%20you%20the%20advantage."
    ; URL with fragment with slashes. Taken from https://github.com/athensresearch/athens/issues/650.
    "https://roamresearch.com/#/app/Joihn_Morabito/page/vICT-WSGC"
    ; Non-lowercase URLs. Taken from
    ; https://en.wikipedia.org/wiki/Template:URL/testcases.
    "HTTPS://www.EXAMPLE.cOm/"
    "https://www.EXAMPLE.cOm"
    "http://www.example.com?foo=BaR"
    ; URL with port.
    "http://www.example.com:8080"
    ; URL with port, path and fragment.
    "http://www.example.com:8080/test123#foobar"
    ; URL with IP address.
    "http://127.0.0.1"
    ; URL with username and password.
    "http://a:b@example.com"))

; Tests for strings that should not be parsed as URLs.
(deftest parser-lone-invalid-raw-url-tests
  (are [text] (= [:block [:paragraph text]] (sut/staged-parser->ast text))
    ; URLs without host.
    "http:///a"
    ;; `#` is special character so is represented by separate string
    ;; "http://#"
    "http://?"
    "http://12345"
    ; TODO(agentydragon): Also should not pass:
    ;   http://0.0.0.0
    ;   http://999.999.999.999
    ; See https://mathiasbynens.be/demo/url-regex for more.
    ))


(deftest parser-url-link-tests
  (are [x y] (= x (sut/staged-parser->ast y))
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


#_(deftest combine-adjacent-strings-tests
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
    (are [x y] (= x (sut/staged-parser->ast y))
      [:block [:paragraph [:latex "text"]]]
      "$$text$$"

      [:block [:paragraph [:latex "text with space"]]]
      "$$text with space$$"))

  (testing "that other syntax is escaped when in LaTeX"
    (are [x y] (= x (sut/staged-parser->ast y))
      [:block [:paragraph [:latex "[[  ]]"]]]
      "$$[[  ]]$$"

      [:block [:paragraph [:latex "[an example](https://example.com/)"]]]
      "$$[an example](https://example.com/)$$"))

  (testing "that LaTeX is not embedded in "
    (are [x y] (= x (sut/staged-parser->ast y))
      [:block
       [:paragraph
        [:link
         {:text "an $$\textLaTeX$$ example",
          :target "https://example.com/"}]]]
      "[an $$\textLaTeX$$ example](https://example.com/)"))

  (testing "that LaTeX expressions can have $ in them"
    (is (= [:block [:paragraph [:latex "a b $ c"]]]
           (sut/staged-parser->ast "$$a b $ c$$")))))


