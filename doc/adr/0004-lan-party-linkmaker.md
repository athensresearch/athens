# 4. Lan-Party Linkmaker

Date: 2021-06-28

## Status

Proposed.

Extends [3. Lan-Party Common Events](0003-lan-party-common-events.md)

## Context

While [3. Lan-Party Common Events](0003-lan-party-common-events.md) addresses structural editing of Knowledge Graph,
we also need a way to maintain linked nature of graph.

We'd like this mechanism to be shared between Single-Player and Lan-Party modes the same way as `common-events`.

Both blocks and pages can be referenced, and are commonly called "refs".
Page titles and block strings can contain refs, represented as a string.

The string format for a page ref is it's title enclosed in double brackets.
The string format for a block ref is it's block uid enclosed in double parenthesis.

Worth keeping in mind that a page title can contain other refs and thus adding a page ref can result in multiple refs.
For instance, the `Foo [[Bar]] ((baz))` page title, when referred to in a string as `[[Foo [[Bar]] ((baz))]]`,
will result in 3 entries in `:block/refs`:
- one ref to the page with title `Foo [[Bar]] ((baz))`
- one ref to the page with title `Bar`
- one ref to the block with uid `baz`

Common events will use existing refs to effect structural changes, including the update of `:block/string` and `:node/title`. 
Linkmaker has as sole responsibility to update `:block/refs` in response to `:block/string` and `:node/title` changes.
Linkmaker never updates either `:block/string` or `:block/title`.

## Decision

**Linkmaker** has following identified requirements:

 - *p1*: page created
   - -> no ref to the page should exist due to *p2* together with *b2*
     - but if it happens, it's handled the same as the *b1* corner case (ignored)
   - -> the page title itself can include references, triggering *m1*
 - *p2*: page deleted
   - -> the event will remove string refs from strings by stripping them of the enclosing double brackets
   - -> triggers *m2*, once for each string changed
 - *p3*: page rename
   - -> the event will replace string refs in strings
   - -> triggers *m1* and *m2*
   - -> new name can include refs itself, adding those to the refs of the affected strings
 - *p4*: page merge
   - -> the event will replace string refs in strings for the merged page
   - -> functionally the same as *p3*
 - *b1*: block creation
   - -> it is possible to have unresolved refs to this block due to *m3*
     - e.g. this case:
       - user inputs `((foo))` in a block string
       - block uid `foo` does not exist in db
       - later block with uid `foo` is added to db
     - checking this on every block creation requires either searching all strings in the db (slow), or
     saving indexed information about unresolved refs on the db (extra complexity)
     - this case is ignored for the sake of simplicity and how infrequent it should be, but can be revisited later
   - -> otherwise functionally the same as *b2*
 - *b2*: block string edit
   - -> block edit event will create missing pages that are referenced
   - -> triggers *m1* and *m2*
 - *b3*: block delete
   - -> the event will delete the block and replaces string refs on strings that ref it with the deleted block string 
   - -> triggers *m2* an potentially *m1* due to replacement with text
 - *m1*: string for uid has new refs
   - -> new refs are added as `[[:block/uid uid] :block/refs _REF_]` 
 - *m2*: string for uid has lost refs
   - -> lost refs are removed from `[[:block/uid uid] :block/refs _REF_]`
 - *m3*: string contains refs that cannot be resolved
   - -> these are ignored and not added to refs
     - this cover cases such as intentional usage of double parens without block uid
     - this can enable instances of the *p1* corner case (page created that is already ref'd)
 - *m4*: db contains broken/missing refs
   - -> linkmaker recomputes all refs for the db

In short, all cases where a string changes (either `:block/string` or `:node/title`) trigger *m1*, *m2*, *m3*, causing a ref delta to be computed.

**Linkmaker** should preserve behavior of `walk-transact`.  
We want to have it tested, so we can rely on it.

## Consequences

**Linkmaker** should be included before transacting.
It should work on Datascript transaction report, so we can give it structural graph edits and it should create
necessary assertions and retractions to maintained linked nature of our Knowledge Graph.
