# 2. Staged parser

Date: 2021-04-04


## Status

Proposed


## Context

Athens current parser is exploding whenever we have to changed things that are allowed.

Examples:

- support for "`" in fenced code blocks
- how we handle raw-url, basically every character in parsed string has to be checked if it might start raw-url.

Current parser is monolithic, which makes it hard to maintain and add new features.

Users are asking for more markdown support.


## Decision

Let's parse things in 3 stages:
1. Block element
2. Inline elements
3. Searching for raw-urls where they are permitted.

This means at least 2 separate parser: block and inline.

Spec used for MD: https://github.github.com/gfm/

Each Phase parser is smaller, so it can run faster and is easier to maintain and test.


### Block elements

List of block elements specified by MD spec:
* Leaf blocks
  * Thematic breaks (like `---`)
  * Headers (only AXT headings for now, that is headers starting with run of `#`)
  * Indented code (contents doesn't get parsed with inline parser)
  * Fenced code (contents doesn't get parsed with inline parser)
  * HTML blocks (let's stay away from that if we can)
  * Link reference definitions (do we want it?)
  * Paragraphs
  * Blank lines
  * Tables (we probably want this extension)
* Container blocks
  * Block quote
  * List items (we probably want those, not sure if we want nested lists)
  * Task list items (extension, since we have `{{[[TODO]]}}` it's not needed)
  * Lists

Only some contents of block elements should be parsed for *Inline elements*.  
We communicate that with `paragraph-text` AST element, which will be processed in Phase 2.

### Inline elements

List of inline elements specified by MD spec:

* Entity and numeric character references
* Code spans
* Emphasis and strong emphasis
* Strikethrough (extension)
* Links (standard `[]()` format)
* Images (standard `![]()` format)
* Autolinks (between `<>`)
* Autolinks extension (basically our current `raw-url`)
* Raw HTML (let's avoid this can of warms)
* Hard line breaks (`  ` or `\` at the end of a line)
* Textual content (anything that didn't match above rules)

To this rules we have to add our own syntax:
* Block references
* Components
* Page links
* Hashtags
* LaTeX


### raw-urls

This is *Autolinks extension* from *Inline elements*.

Having it separate will allow for performance boost.  
For example we don't need to run whole parser for it.  
What we can do instead is to use regex provided by RFC3986.

And we should only process results of Stage 2 that are eligible for containing urls.


## Consequences

Maintaining parser becomes easier, because we have dedicated parser to block & inline.

Extending parser becomes easier, because these will be smaller parser to analyze.

Parsing can be faster.  
Because block level parser is not concerned with inline and raw urls.  
Of course we still need to be careful and test performance impact of changes  
as it is with parser, they do explode (complexity wise) if we're not careful.
