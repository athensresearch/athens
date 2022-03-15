# 24. Reactive DataScript

Date: 2022-03-03


## Status

Implemented


## Context

Athens uses [Posh](https://github.com/denistakeda/posh) to trigger changes in Reagent components when relevant DataScript data changes.

[Frontend Performance Monitoring](0023-frontend-performance-monitoring.md) revealed a significant amount of time was spent inside Posh post-transaction processing, which prompted us to look deeper into our usage of Posh to determine if it was possible to improve performance.

We found that a number of issues:
1. our usage of `datascript.core/reset-conn!` to change database content was very slow when used together with Posh reactions, because it created a very large transaction report that needed to be analysed.
2. many of our queries and pulls looked for much more data than necessary, leading to extra analysis and renders
3. it was very hard to know if a given component was using a reactive pull or query, since `reagent/atom` usage anywhere in a function call stack will register a reaction onto a component


## Decision

We added the `athens.reactive` namespace to function as a single access point to reactive datascript functions.
we moved all instances of data access functions that are meant to be reactive to this namespace, and added functions to inspect the watchers.

This namespace is meant to address issue #3 by convention: you should be explicitely looking for a reactive function to get one.

All functions gathered in this namespace were reviewed for scope, reducing it as much as possible to address issue #2.
`get-reactive-node-document` and `get-reactive-block-document` were converted into non-recursive versions to limit the scope of renders, and block components now watch only their direct children.

Issue #1 was addressed by pausing Posh watchers while resetting the connection.
Posh itself does not contain functions to stop or pause watch, but by inspecting local state we found that watching a different datascript connection will stop watching the previous one.
This observation allowed us to pause watchers by watching an empty connection, reset connection, and then watch the original connection again.

We also employed another mechanism to reduce the impact of watchers: when loading a different database, the loading component is as top-most as possible and there is no other component under it.
By reducing the number of components on screen we reduce the number of component that might be watching state at this time.

Monitoring allowed us to validate that these changes did indeed improve performance, and will allow us to observe if that changes.


## Consequences

Developers will have to call the `athens.reactive` namespace explicitely to create or reference functions that are reactive.

PR reviewers will need to subject PRs that use this namespace or `posh.reagent` to extra scrutiny to prevent performance regressions.

Developers will have to manually add and remove watchers.

Boot and large pages are much faster.

