# x. Multi-platform Client

Date: 2021-08-20


## Status

Pending


## Context

The Athens client is built for two platforms, Electron and Web, using the same compilation unit.
This is possible in CLJS due to the dynamic nature of using Electron dependencies via `js/require`, allowing compilation to finish successfully.

It's common enough for a `js/require` to be included as a toplevel form though, leading the web app to crash on load.
This crash happens because the `js/require` variable is not defined on a web browser, thus leading to a "variable is not defined" error.

The distinction between what's web specific and what's electron specific is not very clear in the code base, facilitating developer mistakes.
Ideally, platform specific code and abstractions would be isolated from non-platform ones.

From a capability point of view, the web platform is mostly a subset of the electron platform.
Electron clients can do everything the web clients can do, and more.
This isn't strictly correct as the Electron client is bound to a Chromium browser, and web clients can run on other browsers and thus have different capabilities.
But we are primarily concerned with Chrome/Chromium as the main client here.


## Proposals

### Platform specific capabilities

When there's a mismatch between capabilities provided by the environment and the capabilities the application is expecting at runtime, an error is thrown.

We can move some of the runtime errors to compile-time errors by using either Shadow-CLJS [Reader Features](https://shadow-cljs.github.io/docs/UsersGuide.html#_conditional_reading) or [Custom Resolvers](https://shadow-cljs.github.io/docs/UsersGuide.html#js-resolve).
These features allow for conditionally loading of clj and js modules.
Under this approach the compilation would fail when trying to make use platform specific modules.
Failures of this type are usually easy to trace because of the detail of compiler errors.
But use of indirection, such as re-frame handler registration, can defer the error back to runtime.

We can keep the errors at runtime by using Clojure conditional expressions, optionally with [Closure Defines](https://shadow-cljs.github.io/docs/UsersGuide.html#_conditional_reading).
The latter allows for improved optimizations of the size of compiled code.
We can also improve the quality of errors by providing our own descriptive error messages on else side of the conditional, such that usage of platform specific functionality outside that platform is signalled clearly.
Under this approach there will be no compile-time platform related error detection.


### Seam between app functionality and platform functionality

It is sensible to confine platform specific functionality to namespaces (e.g. `athens.platform.electron`).
This provides guidance to both implementer and reviewer of how platform features are used in the rest of the application.

We can further this separation by choosing only a subset of app constructs that needs to make use of platform features.
The most obvious construct right now is the Database.
Proposed initial types would be in-memory, local, self-hosted.
It's possible we may also want self-hosted graphs to store data locally, but at this moment it's not clear how and why, and we could always add more types.
Under this model specific database types define how they should be stored, and the app only uses generic interfaces to interact with them.

We could also introduce an additional construct that defines local graph storage IO.
For Electron this would be disk, and for Web this would be one of the persistent storage layers (local storage, indexeddb, service worker cache, others).
This can get further complicated because not all storage provides the same capabilities with regards to notifications and atomicity of operations.
Under this model databases that require local storage could only be used if such storage was available.


### Future functionality

The current pain point is database storage mechanisms, but it is likely we will have other functionality in the future that is only applicable to one client type.
It is hard to put forward a general enough model that would support any client specific capability, instead we should focus on keeping the door open for changing our model later on.

 
## Decision

TDB


### Implementation

TBD


## Consequences

TDB
