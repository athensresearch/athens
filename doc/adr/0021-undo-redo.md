# 21. Undo/Redo

Date: 2022-01-06


## Status

MVP implementation


## Context

Athens RTC does not support undo/redo functionality.
This was deliberately cut to reduce scope since the semantics of undo/redo are non-trivial in multiplayer applications.

In our internal usage we've seen a number of situations where not having undo has resulted in data loss.
Some of these were via deliberate deletion of content, others through accidental deletion via bugs.
In both classes of problems the data would have been recovered easily with undo.

Time travel functionality, where past states of a graph can be seen and used to recover data, sounds like a promising approach to undo/redo that could leverage the [Athens Protocol model of time](doc/adr/0018-athens-protocol-principles.md). 
While related, it does not quite fit: time travel is about visualizing past states, but undo is about reverting operations over a past state.

To further complicate matters, the multiplayer nature of Athens RTC means that undo must take into account interleaved operations from other users.
This is significantly different than in a singleplayer where there is a single canonical "timeline" that does not change aside from the interactions of the current user.

We can find accounts of undo implementations in modern multiplayer product blogs like [Figma's](https://www.figma.com/blog/how-figmas-multiplayer-technology-works/) and [Hex's](https://hex.tech/blog/a-pragmatic-approach-to-live-collaboration). Both
use a key-value property model as the lowest level of operation, and perform undo by reversing those operations.

Figma's undo implementation also lists one very important guiding principle: performing an undo followed by a redo should leave the application in the same state. 
This is crucial when undoing in multiplayer with multiple editors. 
The important implication of this principle is that the redo operation should not simply be the original undone operation, because more might have changed meanwhile, but rather the reverse of the undo operation itself. 

Athens's model of time gives us a starting point for a model of undo: time only moves forward, and thus undoing an operation yield an operation that reverses the effects of the original one, to be applied in a point further in time.

[Operations in Athens](doc/adr/0010-atomic-composite-grapth-operations.md) follow the principle of determinism, where the operation itself carries enough information for all participants to perform the same changes with no ambiguity over the same state.
This property enables us to derive an undo operation from a state together with any operation.

We can reduce the participants to three types: the undo issuer, the server, and other users.
Although in theory any of these could construct the concrete undo operation, it is onerous for the server and other users to keep all possible past states that might be required for undos from anyone, for any point in time.

The prime candidate to keep relevant states is the undo issuer themselves. 
This participant can keep a list of states needed for their own set of undoable operations.
However, this operation is still optimistic in nature, and subject to conflicts.
It can be thought of as saying "this is how to undo the operation given what I know now".

We can also transfer the burden of resolution to the server, achieving a "true" undo since the server itself is the source of truth for the RTC system.
The server would still need a way to keep or recall past states in order to resolve the undo operation.
This approach would require the client issuing the undo to somehow direct the server to undo the operation, and wait for the result, losing the optimistic nature of the change.


## Approach

The primary motivation for undo/redo right now is the prevention of data loss in the moment, and keeping that goal in mind we decided to pursue the first candidate: issuer resolves undo operation.
Higher fidelity reconstruction of past application state, either for inspection or data recovery, is left to future time travel functionality.

For each atomic operation over a db state, we can build a corresponding atomic or composite that undoes it in the context of that db state.
For composite operations, we can compute the undo atomic/composite for each atomic operation in it, maintaining order so that the operations make sense. 

Given that any arbitrary composite operation can be resolved to a composite that undoes it, we can repeat the process to arrive at a redo operation that restores the state prior to effecting the undo. 
There's two subtle details to keep in mind in this description: the context for redo, and the restored state.

In the same way that the original undo was resolved in the context of the db state prior to the operation to be undone, so must the redo be resolved in the context of the db state prior to the operation to be redone.
These two contexts are not the same, as performing an undo "moved" time forward, and operations from other users might have "moved" time forward as well.

The state restored by redo is then not the result of applying the original operation. 
It is the state before the undo operation was applied.
This is consistent with Figma's principle that undo-redo should result in the same state.

This is the list of undo operations for each atomic:
  - undo operation map
      - block
          - :block/new
              - undo is :block/remove for uid
          - :block/save
              - undo is :block/save to previous str
          - :block/open
              - undo is :block/open with inverse bool
          - :block/remove
              - undo is
                  - paste of previous IR for block uid in previous position
                  - :block/save for all the ref in str that were replaced in remove
          - :block/move
              - undo is move to previous position
      - page
          - :page/new
              - undo is :page/remove for title
          - :page/rename
              - undo is :page/rename to previous title
          - :page/remove
              - undo is
                  - is paste of previous IR for title
                  - :block/save for every ref stripped by remove
                  - :shortcut/new + shortcut/move to previous position if any
          - :page/merge
              - undo is 
                  - :page/new to previous title
                  - :shortcut/new + shortcut/move to previous position if any
                  - :block/move to all moved blocks
                  - :block/save to previous string for all blocks affected by rename
      - shortcut
          - :shortcut/new
              - undo is :shortcut/remove
          - :shortcut/remove
              - undo is
                  - :shortcut/new 
                  - :shortcut/move to previous position
          - :shortcut/move
              - undo is shortcut/move to previous position

Each client will hold the last X operations they performed, and the database state they were performed over.
The undo operation is computed only when undo is triggered, not ahead of time.
The saved db state should be updated each time the operation order changes and the optimistic state suffers a rollback.

Since undo operations can trivially generate much larger operations than the original, we must pay special attention to payload limits in the client and server. 
We know some of these limits right now but do not enforce them, so we need to start enforcing them for undo/redo to work reliably.
We can also warn users that some operations cannot be undone according to some heuristic (e.g. number of deleted blocks).

Similarly to limits, undo also puts extra stress on the resolution error cases since it will trivially generate uncommon situations.
We must make sure that invariants are enforced on resolution.
Examples include: cannot rename to an existing page, cannot rename a page that does not exist, etc.

A few of the undo operations rely on a view of the current data via IR (internal representation), which is then converted to a set of operations that create that structure.
Undo again adds extra stress to this functionality via enhanced usage.
We know it's possible to end up with unlinked refs from the order of block creation.
We can address that issue by creating all blocks before adding their content.


## Insights from MVP

- what block should be focused after undo/redo?
- should block/open have undo?
- what are undoable scenenarios and how do we present them to the user?
- what limits do we want to enforce?
- how do we present scenarios that won't allow undo to the user?
- what are the invariants we need to check on op and undo op resolution?
- repeating undo/redo over an operation results in an ever increasing operation due to nested composites
- clients with partial state, derived from partial loads, cannot compute the full undo operation since it doesn't have all the data. 
  In that case it can compute a partial undo, and ask the server to compute the full version.
  Another strategy is to have the partial load client pull all the data necessary for the operation.
- When can optimize the operations in undo by flattening the list and analysing it for redundancy (e.g. block/save followed by block/delete).
  This can help save IO, and we already know that our performance due to IO is suffering.
  This analysis would need to fully understand the causality chain between operations in order to not introduce bugs over valid composites.
  A better place to effect this optimization is in the transaction resolver itself, as an execution planner.
  This would also reduce, or even eliminate, the need for iterative resolution.
- It's not actually mandatory to update the saved db state on client optimistic rollbacks. 
  The difference here is a semantic one: if we update we say undos show use the most up to date data, if we don't we say they should use the original data.
- Contiguous moves are problematic to undo, because of how they implicitely have a grouping together with how they resolve back their previous position.
  We currently have a "forward" bias, where we prefer the :first position if available, followed by :after.
  But while undoing contiguous moves this fails, because undoing reverts the order of operations, and resolves each position relative to the previous block, which is not yet moved, and results in all blocks but the first staying in place.
  There's a range of approaches to this issue: static analysis, changing the bias, encoding contiguous ranges, failing the undo.
  For now we chose to restore the relative order of contiguous moves, after reversing all operations.
  This is not correct for the general case, but it might be sufficient for all the cases we care about, especially since we have decided that undo is not time travel.

## Decision


## Consequences


## Further work
