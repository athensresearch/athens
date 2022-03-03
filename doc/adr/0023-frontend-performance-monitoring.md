# 23. Frontend Performance Monitoring

Date: 2022-03-02

## Status

Proposed

## Context

After implementing Collaborative Knowledge Graph our Graphs started growing very fast,  
which exposed all sorts of performance problems.

To solve the problem in a sustainable way we want to measure and report our performance.

While we know that both Backend and Frontend experiences performance problems,  
we've decided to tackle 1st Frontend performance, because it's experienced all the time.


## Approach

We've decided to capture performance monitoring data in [[Sentry]] for 2 reasons:  
- Because it allows for tracking *spans* (portions) of transactions, not only transactions
- It will allow us to correlate backend performance with frontend performance

We have 3 groups of operations that cost us times on frontend:  
- [[re-frame]] event processing, including effects
- Rendering
- [[DataScript]] updates and reads (those are usually part of previous 1)

Description of the above follows.

### Monitoring (([[re-frame]] event processing, including effects))

The way [[re-frame]] events are advised to be implemented is to return data that represents further processing,  
this usually takes fraction of total event processing time.

Here's successful async event processing  
![](0023-frontend-perf-mon-events-success.png)

Here's failure async event processing  
![](0023-frontend-perf-mon-events-failure.png)


### Monitoring ((Rendering))

We've tried attacking problem of measuring Rendering performance with [[HOC]] (Higher Order Component),  
this leverages [[React.js]] ability to use lifecycle hooks to measure when component is setup and when it's finished rendering,  
which allows us to capture time it took to render.


### Monitoring (([[DataScript]] updates and reads (those are usually part of previous 1)))

We want to monitor timing of all [[DataScript]] usage, because it's our data access layer, which usually is where apps spend most time.  
To do so we've decided to go with `defn` macro.

We did consider using wrapper macro, simpler macro to maintain,  
but it required all call sites to maintain wrapping, a lot of accidental complexity added.


## Insights from MVP

### Implicit transactions

Those are spans that get automatically upgraded to Sentry TXs, because there wasn't TX already present.

We can't implicitly capture transactions, we have to be explicit, because of Sentry TX quota:  
- We've being automatically promoting Sentry span that didn't have running Sentry TX to TX itself.
- This explodes amount of TXs reported and goes over our quota.
- This needs to be reduced significantly, or totally.


### Potential followup: Profiler API

[[React.js]] offers Profiler API, which is also using [[HOC]] approach and it's [onRender Callback](https://reactjs.org/docs/profiler.html#onrender-callback) handles rendering information.  
But it's not clear how we could integrate it with Sentry, because Sentry measures times itself,  
while Profiler API offers summary of execution times.

Profiler API is by default turned off in production builds, if we'd want to use it in production,  
here is how to enable it in prod builds https://gist.github.com/bvaughn/25e6233aeb1b4f0cdb8d8366e54a3977


### Managing Sentry's TXs & spans ain't easy, but it's necessary

In Reagent everything is event driven, pretending like it's asynchronous model,  
but we run on JS, which can only have 1 thread.

To avoid need of passing Sentry TXs & Spans we need make them omnipresent,  
This is why we need to manage them ourselves.

Otherwise we'd have to pass Sentry TX and Span as arguments to every fn call that we want to have access to monitoring.


### Surprising insights is on

Having deep perf monitoring allows us to discover things we have not being looking for  
like the fact that `block-nil-eater` middleware is constant overhead,  
which in hindsight is obvious, and at the same time nobody suspected it.


## Decision

We'll continue monitoring frontend performance.

We've identified [[Frontend Performance Monitoring]] to be ((Musts | Expected needs)) in [[Kano Framework]],  
which means we can't suck at it, but it also makes little sense to be extraordinary at it.

## Consequences

We get to see how Athens performs for end users.  
And we get to see that continuously.
