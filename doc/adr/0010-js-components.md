# 10. JS Components

Date: 2021-08-23

## Status

Proposed.


## Context

Inspired by [David Nolen's ClojureScript in the Age of TypeScript](https://vouch.io/developing-mobile-digital-key-applications-with-clojurescript/) talk and post, we started thinking about how viable it would be to use JS (instead of CLJS) components.

This change would provide design with a better and more familiar development environment for components and reduce their dependency on engineering to ship work.
The separation also has several second order benefits related to organization and communication. 

Costs for this change are centered around the degree of separation between application and components, where cljs is the primary language on the former and js on the latter, and maintaining extra tooling.

Part of [Ongoing Hypothesis](https://docs.google.com/document/d/18ExzXHB5aezyINmIVWDBpZpXgV67kAhuAO8MvX6dbPw/edit).


## Decision

Decided to use JS components with Storybook, compiled from TSX, after seeing how much Stuart was able to get done with them.


## Consequences

Negative consequences:
* JS/CLJS context switching when working in the app and on components at the same time
* Higher surface area for JS interop problems
* More complex build step
* More tooling to maintain
* Harder for for CLJS engineers to develop and maintain components


Positive consequences:
* Better development and testing environment for components
* Clearer defined deliverable scope and context for components
* Component documentation
* Smaller and simpler application codebase 
* Looser coupling between engineering and design
* Design can deliver work on separate cadence, units, and timeline, from engineering
* Easier to enforce discipline on components as pure functions
* CLJS proficiency is not necessary for design work
* Easier for contributors to contribute to components
* Reduced bus factor in design

Deferred work items:
* The [devcards style guide](https://github.com/athensresearch/athens/blob/c697b7c62d60dd9fca0b03a32b35cc4776a90c54/src/cljs/athens/devcards/style_guide.cljs) will need to be reimplemented in Storybook
* The [devcards stylify guide conventions](https://github.com/athensresearch/athens/blob/c697b7c62d60dd9fca0b03a32b35cc4776a90c54/src/cljs/athens/devcards/styling_with_stylefy.cljs) will need to be carried over to Storybook equivalents
* The [partial work on filters](https://github.com/athensresearch/athens/blob/feature/rtc-v1/src/cljs/athens/views/filters.cljs) should be used as reference for a future filters implementation
