# X. JS Components

Date: 2021-08-23

## Status

Proposed.


## Context

Inspired by [David Nolen's ClojureScript in the Age of TypeScript](https://vouch.io/developing-mobile-digital-key-applications-with-clojurescript/) talk and post, we started thinking about how viable it would be to use JS (instead of CLJS) components.

This change would provide design with a better and more familiar development environment for components and reduce their dependency on engineering to ship work.
The separation also has several second order benefits related to organization and communication. 

Costs for this change are centered around the degree of separation between application and components, where cljs is the primary language on the former and js on the latter, and maintaining extra tooling.


## Decision

TBD


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
