(ns athens.parser-test
  (:require
    [athens.parser :refer [parse-to-ast parse-to-ast-new combine-adjacent-strings]]
    [clojure.test :refer [deftest is are testing]]
    [instaparse.core :as insta]))


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


(deftest parser-raw-url-tests
  (are [x y] (= x (parse-to-ast y))
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
  (are [url] (= [:block [:url-link {:url url} url]] (parse-to-ast url))
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
  (are [text] (= [:block text] (parse-to-ast text))
    ; URLs without host.
    "http:///a"
    "http://#"
    "http://?"
    "http://12345"
    ; TODO(agentydragon): Also should not pass:
    ;   http://0.0.0.0
    ;   http://999.999.999.999
    ; See https://mathiasbynens.be/demo/url-regex for more.
    ))


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

    [:block [:page-link "January 1, 2021"] " ok"]
    "[[January 1, 2021]] ok"

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

    ;; mode detection & multiline
    [:block [:block-pre-formatted "(ns example)\n(def a 1)" "clojure"]]
    "```clojure\n(ns example)\n(def a 1)```"

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
    (are [x y] (= x (parse-to-ast-new y))
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
      "https://raw-link.com"

      [:block "Something/and-hyphen"]
      "Something/and-hyphen"))

  (testing "invalid cases"
    (are [x] (true? (insta/failure? (parse-to-ast-new x)))
      ;; TODO: it's probably better to return input string with parse error data
      ;; [:block [:url-link {:url "https://example.com/"} "no #hashtag or [[link]] inside"]]
      ;; "[no #hashtag or [[link]] inside](https://example.com/)"
      "[no #hashtag or [[link]] inside](https://example.com/)"

      ;; TODO: do we really want to support escaped `]` inside of `[]` block
      "[escaped \\](#not-a-link)](https://example.com/)")))


(deftest parse-new-latex-tests
  (testing "that LaTeX syntax is detected"
    (are [x y] (= x (parse-to-ast-new y))
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


(deftest parser-formatting-tests

  (testing "bold formatting"
    (are [x y] (= x (parse-to-ast-new y))
      [:block [:bold "test"]]
      "**test**"

      [:block "abc " [:bold "def"]]
      "abc **def**"

      [:block [:bold "abc "] "def"]
      "**abc **def"

      [:block "abc" [:bold "def"] "ghi"]
      "abc**def**ghi"))

  (testing "italic formatting"
    (are [x y] (= x (parse-to-ast-new y))
      [:block [:italic "test"]]
      "*test*"

      [:block "abc " [:italic "def"]]
      "abc *def*"

      [:block [:italic "abc "] "def"]
      "*abc *def"

      [:block "abc" [:italic "def"] "ghi"]
      "abc*def*ghi"))

  (testing "strikethrough formatting"
    (are [x y] (= x (parse-to-ast-new y))
      [:block [:strikethrough "test"]]
      "~~test~~"

      [:block "abc " [:strikethrough "def"]]
      "abc ~~def~~"

      [:block [:strikethrough "abc "] "def"]
      "~~abc ~~def"

      [:block "abc" [:strikethrough "def"] "ghi"]
      "abc~~def~~ghi"))

  (testing "underline formatting"
    (are [x y] (= x (parse-to-ast-new y))
      [:block [:underline "test"]]
      "--test--"

      [:block "abc " [:underline "def"]]
      "abc --def--"

      [:block [:underline "abc "] "def"]
      "--abc --def"

      [:block "abc" [:underline "def"] "ghi"]
      "abc--def--ghi"))

  (testing "highlight formatting"
    (are [x y] (= x (parse-to-ast-new y))
      [:block [:highlight "test"]]
      "^^test^^"

      [:block "abc " [:highlight "def"]]
      "abc ^^def^^"

      [:block [:highlight "abc "] "def"]
      "^^abc ^^def"

      [:block "abc" [:highlight "def"] "ghi"]
      "abc^^def^^ghi")))


(deftest parser-new-raw-url-tests
  (are [x y] (= x (parse-to-ast-new y))
    [:block [:url-link {:url "https://example.com"} "https://example.com"]]
    "https://example.com"

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


(deftest parser-new-lone-raw-url-tests
  (are [url] (= [:block [:url-link {:url url} url]] (parse-to-ast-new url))
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


(deftest parser-new-lone-invalid-raw-url-tests
  (are [text] (= [:block text] (parse-to-ast-new text))
    ; URLs without host.
    "http:///a"
    "http://#"
    "http://?"
    "http://12345"
    ; TODO(agentydragon): Also should not pass:
    ;   http://0.0.0.0
    ;   http://999.999.999.999
    ; See https://mathiasbynens.be/demo/url-regex for more.
    ))
