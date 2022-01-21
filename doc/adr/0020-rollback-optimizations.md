# 20. Rollback Optimizations

Date: 2021-12-02


## Status

MVP


## Context

The Athens client optimistically resolves and applies events to its internal database state for responsiveness.

When the client receives events that invalidate the optimistic state it must return to a valid state.
Currently, for each event that invalidates the state, we reset to the last valid state all, apply the event, and then reapply all the optimistic changes.

This approach is tauntamount to a full database reset does not allow for incremental subscription updates.
For non-trivial databases,`posh` (datascript watcher for re-frame) will re-run all subscriptions, and thus all queries, over datascript and freeze the app for a few seconds.


## Approach

We've identified two promising approaches:
- batch processing for events that cause rollbacks
- perform incremental rollbacks

The first approach requires accumulating events before processing them, either by count or time.
A time based approach based on a debounce mechanism, such as [`goog.debounce`](https://google.github.io/closure-library/api/goog.functions.html), sounds like the most straightforward.
It is unclear how to perform an idiomatic debounce in re-frame. 

The second approach requires a way to undo existing optimistic changes instead of resetting to a previous state.
This can be achieved by storing the datoms from the transaction report for each change.
These datoms contain a boolean that indicates whether the datom as added or removed, and by flipping the boolean we obtain a valid transaction that undoes the original.
After undoing all optimistic changes, each new event is applied, followed by the optimistic changes.
We then again store the transaction report from the reapplication of optimistic changes, since these are now new reports.


## Insights from MVP

We started getting frequent freezes for seconds or even minutes.
Profiling shows `posh` seemed to be causing some of the freezes.
We settled on following the second approach for the MVP and are observing the results.
If indeed the cause of freezing is due to resetting the database, this approach should completely eliminate it.


## Decision


## Consequences
