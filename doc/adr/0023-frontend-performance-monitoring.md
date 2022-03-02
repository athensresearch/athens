# 23. Frontend Performance Monitoring

Date: 2022-03-02

## Status

Proposed

## Context

- After implementing Collaborative Knowledge Graph our Graphs started growing very fast,
which exposed all sorts of performance problems.
- To solve the problem in a sustainable way we want to measure and report on performance.
- While we know that both Backend and Frontend experiences performance problems,
we've decided to tackle 1st Frontend performance, because it's experienced all the time.

## Approach

- For now we've decided to capture performance monitoring data in [[Sentry]]
    - Because it allows for tracking *spans* (portions) of transactions, not only transactions
    - It will allow us to correlate backend performance with frontend performance
- We have 3 groups of operations that cost us times on frontend:
    - [[re-frame]] event processing, including effects
    - Rendering
    - [[DataScript]] updates and reads (those are usually part of previous 1)
- Monitoring (([[re-frame]] event processing, including effects))
    - The way [[re-frame]] events are advised to be implemented is to return data that represents further processing
        - This usually requires fraction of real event processing time
        - Successful async event processing
            - ![](0023-frontend-perf-mon-events-success.png)
        - Failure async event processing
            - ![](0023-frontend-perf-mon-events-failure.png)
- Monitoring ((Rendering))
    - We've tried attacking problem of measuring Rendering performance with [[HOC]]
    - This leverages [[React.js]] ability to use lifecycle hooks to measure when component is setup and when it's finished rendering
- Monitoring (([[DataScript]] updates and reads (those are usually part of previous 1)))
    - We want to monitor timing of all [[DataScript]] usage, because it's our data access layer
    - To do so we've decided to go with `defn` macro
        - We did consider using wrapper macro, simpler macro to maintain
        - But it required all call sites to maintain wrapping, a lot of accidental complexity added
        - And value is in capturing all data access layer usage

### Insights from MVP

- We can't implicitly capture transactions, we have to be explicit, because of quota
    - We've being automatically promoting Sentry span that didn't have running Sentry TX to TX itself
    - This explodes amount of TXs reported and goes over our quota
    - This needs to be reduced significantly, or totally
- [[React.js]] offers Profiler API, which is also using [[HOC]] approach and it's [onRender Callback](https://reactjs.org/docs/profiler.html#onrender-callback) handlers rendering information
    - Thought it's not clear how we could integrate it with Sentry
        - Because Sentry measures itself times, while Profiler API offers summary of execution times
    - Profiler API is by default turned off in production builds
        - This is how to enable it in prod builds https://gist.github.com/bvaughn/25e6233aeb1b4f0cdb8d8366e54a3977
- Managing Sentry TX & span ain't easy, but it's needed
    - In Reagent everything is event driven, pretending like it's asynchronous model
        - But we run on JS, which can only have 1 thread
    - To avoid need of passing Sentry TXs & Spans we need make them omnipresent
    - This is why we need to manage them ourselves
- Having deep perf monitoring allows us to discover things we have not being looking for
    - Like the fact that `block-nil-eater` middleware is constant overhead
        - Which in hindsight is obvious, and at the same time nobody suspected it

## Decision

- We'll continue monitoring frontend performance
- We've identified [[Frontend Performance Monitoring]] to be ((Musts | Expected needs)) in [[Kano Framework]]
    - Which means we can't suck at it, but it also makes little sense to be extraordinary at it

## Consequences

We get to see how Athens performs for end users.
