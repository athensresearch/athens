# event-sync

EventSync is a multi-writer event log synchronizer model.
This is useful for event-based distributed system architectures that want to apply events optimistically while synchronizing them in the background.

It gives you a way to reason about event synchronization, inspect the current synchronization state, and reactively perform synchronization actions that ensure each writer's events remain ordered.

It does not give you a way to represent any state besides the event logs being synchronized, a way to save events, or to handle semantic conflicts.


## The problem, and the approach

TODO: event archs, sync, local state, offline first


## Model

The core idea of EventSync is that you can represent events across multiple synchronizing logs as if it was a single concatenated log.

The resulting log can have new events inserted in the middle of the log.
This situation represents events from other writers that arrived at that log before existing events from the current writer.
Events from each writer are guaranteed to preserve their relative order.

The important terms in this document are:
- Event: this is an event in your system that you want to synchronize.
- Stage: a log of events that you want to synchronize with another one such log.
- Event ID: a unique identifier for an event across all stages.
- Order number: a monotonically increasing number for each event within a stage.
- Insert: operation that adds a new event to stage.
- Remove: operation that removes the oldest event from a stage.
- Promote: operation that removes from a stage while inserting it in the next stage.
- State: a self-consistent view of events by stage, and of the last operation.
- State atom: a Clojure atom containing the changing state, over which you can perform operations.
- Log: ordered sequence of unique events across all stages in a state.
- Application state: state maintained by an application when interpreting events.
- Subscription: an ordered stream of events from a log starting at a given order number.

As a writer, you start by creating new EventSync state atom with one stage for each of your logs, and setup subscriptions for each of your logs.

Whenever your subscriptions show a new event was added to a log, you add it to the matching stage in the EventSync state atom.
The EventSync API will determine if it's an insert or promote.

Each time the atom changes you look at the last operation to decide what to do in your application:
- promotions and insertions mean you need to save that event to the next stage
- insertions and removals mean you need to check if the sync log and update your application state according to the new log

Notably, application state does not need to be updated on promotion because, by definition, the log will not change.
The event moved from one stage to the next, but its order on the log remains the same.


## Examples

To get a feel for what synchronization with EventSync looks like, let's look at a series of examples.

In these examples we have three stages (in-memory, local storage and server) and two writers (Alice and Bob).
Bob is not connected to the same local storage as Alice, but both are connected to the same server.
The examples happen one after the other, but you can read them separately.

This is a common set of stages for an offline-first browser application, but you can imagine different sets of stages.
An application without any offline capabilities would only have an in-memory and server stages.
You can also have an application that only synchronizes between two servers, or even chain multiple applications using the EventSync model.


### Describing states

We can talk about EventSync by representing the sequence of states each writer has.
Each state looks like this:

```
Writer ID              : Alice
Log                    : a4 a3 b1 a2 a1
Stage 1 - In-memory    : a4
Stage 2 - Local storage: a3 b1 a2
Stage 3 - Server       : a1
Last operation         : promote 2 a3
Operation count:       : 15
```

In this description events are represented by their event ID and ordered from newest (left-most) to oldest (right-most) within a stage and on the log.
The operation shows its name, followed by stage it operated over and the event ID.
The operation count serves as a notion of time for this state within its state atom, in this case 15 operations have happened.

Event names in these examples are the first letter of the writer name followed by the order number the writer inserted them in.
Thus `a3` means this is the third event that Alice inserted.
This format is meant to make it easy to read and reason about the events.
Real event IDs can be anything, as long as they are unique.

A starting state has no events in any stage and its last operation was initialization:

```
Writer ID              : Alice
Log                    :
Stage 1 - In-memory    :
Stage 2 - Local storage:
Stage 3 - Server       :
Last operation         : initialization
Operation count        : 0
```


### Something simple

Both Alice and Bob start with the same state, except for the Writer ID: 

Alice starts by adding an event to the first (in-memory) stage.

```
Writer ID              : Alice
Log                    : a1
Stage 1 - In-memory    : a1
Stage 2 - Local storage:
Stage 3 - Server       :
Last operation         : insert 1 a1
Operation count        : 1
```

Alice sees the insertion, and saves that event to the second (local storage) stage.
Alice also sees the log has changed, and thus it should update its application state with `a1`.

When Alice's subscription to local storages shows that `a1` arrived, she promotes it:

```
Writer ID              : Alice
Log                    : a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: a1
Stage 3 - Server       :
Last operation         : promote 2 a1
Operation count        : 2
```

This repeats itself with the third (server) stage:

```
Writer ID              : Alice
Log                    : a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: 
Stage 3 - Server       : a1
Last operation         : promote 3 a1
Operation count        : 3
```

The last two states did not change the log, so Alice did not need to update application state.

Meanwhile, Bob's subscription sees `a1` was added to the server:

```
Writer ID              : Bob
Log                    : a1
Stage 1 - In-memory    :
Stage 2 - Local storage:
Stage 3 - Server       : a1
Last operation         : insert 3 a1
Operation count        : 1
```

This is the first change that Bob sees because he isn't connected to the same local storage as Alice.
Bob will need to update his application state with it.


### Concurrency

Both Alice and Bob inserted an event roughly at the same time:

```
Writer ID              : Alice
Log                    : a2 a1
Stage 1 - In-memory    : a2
Stage 2 - Local storage: 
Stage 3 - Server       : a1
Last operation         : inset 1 a2
Operation count        : 4
```

```
Writer ID              : Bob
Log                    : b1 a1
Stage 1 - In-memory    : b1
Stage 2 - Local storage:
Stage 3 - Server       : a1
Last operation         : insert 1 b1
Operation count        : 2
```

Although we say "at the same time" what matters here is not so much that they happened at the same real-world time, but rather at the same logical time.
As far as the state in each application is concerned, their own events were inserted before the other one.

This can happen for many known reasons: it really was the same real-time, the network was slow, the computer was slow, the application was slow, the application is bugged, the server was slow, the local storage was slow, one or both writers were offline, there was a network partition, etc.
There's also unknown reasons for this happening.
Concurrent events in a distributed system can be minimized but they can never be eliminated.

Bob's event goes through local storage and arrives at the server first:

```
Writer ID              : Bob
Log                    : b1 a1
Stage 1 - In-memory    :
Stage 2 - Local storage: b1
Stage 3 - Server       : a1
Last operation         : promote 2 b1
Operation count        : 3
```

```
Writer ID              : Bob
Log                    : b1 a1
Stage 1 - In-memory    :
Stage 2 - Local storage: 
Stage 3 - Server       : b1 a1
Last operation         : promote 3 b1
Operation count        : 4
```

Alice sees Bob's event before her own `a2` reached local storage:

```
Writer ID              : Alice
Log                    : a2 b1 a1
Stage 1 - In-memory    : a2
Stage 2 - Local storage: 
Stage 3 - Server       : b1 a1
Last operation         : insert 3 b1
Operation count        : 5
```

For Alice, this insertion changed the order of the log from `a2 a1` to `a2 b1 a1`.
It's up to Alice's application to decide what to do with this changed order.

Here's a couple of options:
- replay all the events in the log and thus rebuild her application state from scratch.
- if she has an intermediate application state saved after resolving `a1`, she can use it to replay only `a2 b1` on top of it.
- decide that it's ok to resolve  `b1` on top of the previous `a2 a1` instead of following the real order.
- defer this decision until later in order to reduce the number of computations being done right now.

Whatever Alice decides to do with the application state will not affect the EventSync state.
It will continue to sync `a2` to the server:

```
Writer ID              : Alice
Log                    : a2 b1 a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: a2
Stage 3 - Server       : b1 a1
Last operation         : promote 2 a2
Operation count        : 6
```

```
Writer ID              : Alice
Log                    : a2 b1 a1
Stage 1 - In-memory    :
Stage 2 - Local storage: 
Stage 3 - Server       : a2 b1 a1
Last operation         : promote 3 a2
Operation count        : 7
```

At which point Bob will also see `a2`, and end up with the same log as Alice:

```
Writer ID              : Bob
Log                    : a2 b1 a1
Stage 1 - In-memory    :
Stage 2 - Local storage: 
Stage 3 - Server       : a2 b1 a1
Last operation         : insert 3 a2
Operation count        : 5
```


### Alice is Offline

Alice has written a few new events (omitted for brevity), but they don't seem to be reaching the server:

```
Writer ID              : Alice
Log                    : a5 a4 a3 a2 b1 a1
Stage 1 - In-memory    :
Stage 2 - Local storage: a5 a4 a3
Stage 3 - Server       : a2 b1 a1
Last operation         : promote 2 a5
Operation count        : 13
```

The `a5 a4 a3` events are stuck in the local storage stage.

It looks like Alice is offline.
It might also be that the server itself is offline.
Or maybe something is just slow.
In fact, offline is indistinguishable from slow after waiting for whatever is considered a reasonable amount of time.

But Alice can continue work and write new events on top of the last known state, and they will be saved at least up to the local storage state.

```
Writer ID              : Alice
Log                    : a7 a6 a5 a4 a3 a2 b1 a1
Stage 1 - In-memory    : a7 a6
Stage 2 - Local storage: a5 a4 a3
Stage 3 - Server       : a2 b1 a1
Last operation         : promote 1 a7
Operation count        : 15
```

```
Writer ID              : Alice
Log                    : a7 a6 a5 a4 a3 a2 b1 a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: a7 a6 a5 a4 a3
Stage 3 - Server       : a2 b1 a1
Last operation         : promote 3 a7
Operation count        : 17
```

Meanwhile Bob still seems to be online, and has been writting his own events that Alice has not yet seen:

```
Writer ID              : Bob
Log                    : b4 b3 b2 a2 b1 a1
Stage 1 - In-memory    :
Stage 2 - Local storage: 
Stage 3 - Server       : b4 b3 b2 a2 b1 a1
Last operation         : insert 3 b4
Operation count        : 14
```

Alice and Bob see different things because Alice hasn't yet gotten the latest events from the server. 


### Two offline Alices

While offline, Alice opened another instance of the application.
This instance is connected to the same local storage.

Even though both instances are controlled by the same person, they are different instances.
Let's call the second one Elsa instead.

```
Writer ID              : Elsa
Log                    :
Stage 1 - In-memory    :
Stage 2 - Local storage:
Stage 3 - Server       :
Last operation         : initialization
Operation count        : 0
```

Even though Elsa is offline, her server subscription keeps a cache so she's able to see the events Alice had seen.
Together with the local storage subscription, Elsa gets up to the same log as Alice.

```
Writer ID              : Elsa
Log                    : a7 a6 a5 a4 a3 a2 b1 a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: a7 a6 a5 a4 a3
Stage 3 - Server       : a2 b1 a1
Last operation         : promote 3 a7
Operation count        : 8
```

When Elsa writes `e1`, Alice will be able to see it via the local storage subscription.

```
Writer ID              : Elsa
Log                    : e1 a7 a6 a5 a4 a3 a2 b1 a1
Stage 1 - In-memory    : e1
Stage 2 - Local storage: a7 a6 a5 a4 a3
Stage 3 - Server       : a2 b1 a1
Last operation         : insert 1 e1
Operation count        : 9
```

```
Writer ID              : Elsa
Log                    : e1 a7 a6 a5 a4 a3 a2 b1 a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: e1 a7 a6 a5 a4 a3
Stage 3 - Server       : a2 b1 a1
Last operation         : insert 2 e1
Operation count        : 10
```

```
Writer ID              : Alice
Log                    : e1 a7 a6 a5 a4 a3 a2 b1 a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: e1 a7 a6 a5 a4 a3
Stage 3 - Server       : a2 b1 a1
Last operation         : insert 2 e1
Operation count        : 18
```

Both Alice and Elsa are up to date with each other, but they are not synced with Bob.


### Back online

Alice and Elsa are back online and started to get events from the server:

```
Writer ID              : Alice
Log                    : e1 a7 a6 a5 a4 a3 b4 b3 b2 a2 b1 a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: e1 a7 a6 a5 a4 a3
Stage 3 - Server       : b4 b3 b2 a2 b1 a1
Last operation         : insert 3 b4
Operation count        : 21
```

```
Writer ID              : Elsa
Log                    : e1 a7 a6 a5 a4 a3 b4 b3 b2 a2 b1 a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: e1 a7 a6 a5 a4 a3
Stage 3 - Server       : b4 b3 b2 a2 b1 a1
Last operation         : insert 3 b4
Operation count        : 13
```

Bob's events changed the order of Alice and Elsa's log.
It's up to their applications to decide what to do with this changed order.

The server subscription shows each of the events sent from Alice and Elsa's local storage stage:

```
Writer ID              : Alice
Log                    : e1 a7 a6 a5 a4 a3 b4 b3 b2 a2 b1 a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: 
Stage 3 - Server       : e1 a7 a6 a5 a4 a3 b4 b3 b2 a2 b1 a1
Last operation         : promote 3 e1
Operation count        : 27
```

```
Writer ID              : Elsa
Log                    : e1 a7 a6 a5 a4 a3 b4 b3 b2 a2 b1 a1
Stage 1 - In-memory    : 
Stage 2 - Local storage: 
Stage 3 - Server       : e1 a7 a6 a5 a4 a3 b4 b3 b2 a2 b1 a1
Last operation         : promote 3 e1
Operation count        : 20
```

```
Writer ID              : Bob
Log                    : e1 a7 a6 a5 a4 a3 b4 b3 b2 a2 b1 a1
Stage 1 - In-memory    :
Stage 2 - Local storage: 
Stage 3 - Server       : e1 a7 a6 a5 a4 a3 b4 b3 b2 a2 b1 a1
Last operation         : insert 3 e1
Operation count        : 20
```

Alice and Elsa's log did not change as their events reached the server.
Bob's log did not change the order of previous events, but got new events.

Alice, Bob, and Elsa are all synced to the same log.


## Requirements

To use EventSync you need ensure:
- create Event IDs, e.g. a UUID generator or equivalent.
- idempotency of event saves to a stage's log, because multiple EventSync writers can be writing the same event.
- monotonically increasing ordering of events within a stage's log.
- subscription capability over a stage's log.

While not a strict necessity, the order number within a stage's log events is very useful to ensure subscriptions can remain partial, and to detect how out of date caches are.

Additionally, for offline-first applications, you also need to ensure a local cache for each non-local log.
This cache will need to store the all events in that stage that are not known to have been removed, and be able to retrieve on subscription.
Without it you will not be able restart the application and get back to the same EventSync state.

You can use extra stages to sync to these caches.
In the example above, you could cache the server events by adding `Stage 4 - Server local cache`.
This would give you the same sync semantics as before.


## FAQ

### How can I detect if EventSync is losing events?

EventSync guarantees that each event from a given writer remain ordered on each stage.
You can keep track of the last event by the writer on the application, and pass the id of previous event on each event.
This allows you to query any log to see if the previous log is there as well.

It's still possible to lose data whenever data deleted from any given stage's log if there's a live subscription that hasn't seen it yet.
This is a strong argument for immutability of stored data, but impractical for caches.
If you delete data from any log or cache make sure it's not pending a subscription read.

maybe error on promoting non-tip?


### How can I handle conflicts?

TODO: only semantic events, not at ES level


### Should I subscribe to all events for each stage on startup?

TODO: need some kind of tracking on each stage store of last one that was verified to be saved
TODO: also detect already saved on startup? hard problem, might never show up if very old but already saved


### Why do I need to wait for the subscription to show an event after saving it to the log?

Besides signalling that an event is present on a stage, the subscription also shows what the ordering of events was.
The latter is what's really important.

Consider the [Back online](#back-online) example.
When Alice came back online, she had to send 6 events from herself and Elsa to the server, and receive from the server Bob's 3 events.
Even though her events might be saved already, for the purpose of ordering the logs they can only be removed from a stage after the next stage shows it in the correct order.


### Won't the last stage accumulate events forever?

TODO: remove events when incorporated into app state to keep it small.


### How many stages should I have?

One for in-memory only events if this application will write events, plus one for each persistent log you have.


### How do I save events between stages?

TODO: watch atom, check last op, make own save fns that ensure idempotency


### How do I recover if a stage is unresponsive?

TODO: hard, considerations below

stop syncing, make a new ES without that stage, start listening to the stages again?
-no, this loses the events in that stage, can't read them... need some kind of cache in local if we never want to lose them
-same problem as restart and stage is gone
-even for the purpose of own events sync, you will lose events like this... maybe keep recording of which hit the last stage?

drain then restart?
- then just back to generic problem on reload if a stage is gone
- stage cache makes more sense then

maybe no restart at all, just backed up stuff?
- then you just have the reload problem

maybe do nothing, just infinite lag?

cache on further stages also ensures on load you see everything right
- if caches are busted, then maybe you have a problem
- but that's actually the real problem, that losing any data is bad, and the answer is not to duplicate it further

failover can just mean that stage auto-promotes to next stage via the subs itself
- e.g. any write to it just shows up on its sub, thus moving it to the next stage

need to figure out better model of thinking about subs to prevent complicated problems on intermediate stages not having some events


### Is it possible for an event to be in multiple stages at once?

TODO: possible for concurrent clients to be sending events on weird cadence? think not, bake into test, maybe make invariant
TODO: log should not show repeats anyway


### What happens is a stage refuses an event?

TODO: API denied save in a non-recoverable manner, tricky case


### Synchronization is hard, how is EventSync tested for robustness?

TODO: generative tests, previous event tracking


### What things to I have to look out for when setting up subscriptions?

intermediate stage subs needs to start at the first event that's not on subsequent stages
- otherwise you can run into cases where you're waiting for subsequent subs to tell you something is there, but that will never happen
- principle is sub needs to start at last unseen event from further stages
- also some considerations here of when to clear the local caches for non-local stages (i.e. when SOT gives you a new starting point)


### Can I batch event saving and subscriptions?

The specific semantics of how events are saved and retrieved are up to your code.
You can batch operations as long as you end up calling the EventSync API.

