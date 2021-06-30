# 4. Lan-Party Linkmaker

Date: 2021-06-28

## Status

Proposed.

Extends [3. Lan-Party Common Events](0003-lan-party-common-events.md)

## Context

While [3. Lan-Party Common Events](0003-lan-party-common-events.md) addresses structural editing of Knowledge Graph,
we also need a way to maintain linked nature of graph.

We'd like this mechanism to be shared between Single-Player and Lan-Party modes the same way as `common-events`.

## Decision

**Linkmaker** has following identified requirements:

 - *p1*: page created
   -  -> check if something refers to it, update refs
 - *p2*: page deleted
   - -> do we need to update `:block/refs`, since we're deleting page entity, probably not
   - -> also check *b6* for all child blocks
 - *p3*: page rename
   - -> find references to old page title, update blocks with new title, update refs
   - -> also check if something refers to new title already, update refs
 - *p4*: page merge
   - -> blocks stay the same, old page refs and page links need to be updated
 - *b1*: block has new page ref
   - -> update page refs
 - *b2*: block doesn't have page ref anymore
   - -> update page refs
 - *b3*: block has new block ref
   - -> update target block refs
 - *b4*: block doesn't have block ref anymore
   -  -> update target block refs
 - *b5*: block created
   - -> check *b1*
   - -> check *b3*
 - *b6*: block deleted
   - -> check *b2*
   - -> check *b4*

**Linkmaker** should preserve behavior of `walk-transact`.  
We want to have it tested, so we can rely on it.

## Consequences

**Linkmaker** should be included before transacting.
It should work on Datascript transaction report, so we can give it structural graph edits and it should create
necessary assertions and retractions to maintained linked nature of our Knowledge Graph.
