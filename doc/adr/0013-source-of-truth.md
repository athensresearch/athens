# 13. Source of Truth

Date: 2021-10-13


## Status

Accepted.


## Context

We've moved all of our UI transaction centric events to semantic events, where we express the intent of the change rather that the database transaction representation.

The Athens server and client both resolve data representations from these events and apply it to their local database, discarding the semantic event after resolution. 
This resolution loses information, because the semantic event contains more than just the applied data, and is specific to the particular database solution used at the time.
But the concrete running local database state constitutes the usable running system.

The core of this problem is what constitutes the source of truth for the knowledge graph.
The two candidates we have for source of truth are the database state and the the total sequence of events.

Choosing a source of truth does not mean we will exclusively use it, and remove any usage of the other mechanism.
Instead, the choice of source of truth informs how we should reason about the system and how should the system achieve desirable properties (e.g. resilience, performance).

Athens remains a multi-user experience where each user is able to collaborate in real time, and where data should be retained for a long time.
The decision will need to provide solutions for communication of data, both in the whole and incrementally, and for handling large amounts of data.


## Decision

To better understand the shortcomings of each approach let's look at them in isolation.
Were we to use the database state as only source of truth, with no notion of incremental changes, we'd have to transmit at least partial snapshots on every change to all clients.
Conversely, without a notion of state, loading a new client would require replaying the total sequence of events.
Each approach individually is insuficient.

The real-time collaboration imposes a hard requirement efficient state updates on clients, which in turn requires accurate incremental changes.
By accurate, we mean that applying the incremental changes upon a state should leave all clients in the same state.
Without this property clients would see increasingly different states, and client actions would not be correct.

This property applies all the way to the initial empty state, which means that the accurate incremental changes requirement in effect means that a total sequence of events must exist and deterministically reduce to the final state.
This in turn also means that any database state can be derived from the sequence of events.
The opposite is not true, as the sequence of events cannot be derived from the resulting database state due to the loss of information described in the context above.
This leaves us with the sequence of events as the more natural source of truth.

Instead of discarding events after resolution, we can store them permanently and achieve some of the benefits of [Event Driven Architectures](https://en.wikipedia.org/wiki/Event-driven_architecture), especially around CQRS and Event Sourcing.
Achieving full CQRS or Event Sourcing is not a goal in and of itself.

Major benefits in our case include:

- ease of migration, between current databases and future ones
- correctness checking, between two resolution implementations
- debugging, via replaying history of changes to runtime
- self-healing of databases, by replaying events with fixed resolutions
- decoupling of event generation and consumption, allowing more sophisticated async and offline-first usecases
- storage, for ephemeral databases

We have decided to add an event log to the Athens server, to which we record every database-affecting operation.

This event log will be implemented as an immutable append-only log in [Fluree](https://flur.ee/).

Fluree was chosen because it offers a Datalog-adjacent query/transaction format, has a matching open-source licence we can use for the server (via Docker), good scalability, and a migration path to a cloud-native implementation. 

Another relevant reason is that Fluree is a technology the team has been interested in experimenting with since it feels a good fit for the long-term Athens use case, so getting some experience with it on a very limited domain is valuable.

It is impractical to always load the full log for large enough logs.
Using state snapshots can help reduce this problem, given they function as a cache for replaying a set of logs.
State snapshots can be used on the server, client, and as an export format to interact with other tools.


## Consequences

Besides enabling the scenarios described in [context](#context), there are also negative, or at least binding, consequences: 

- total time to effect changes increases, because the server needs to store the events before processing it
- the current event format becomes a frozen API that we need to support indefinitely, since all events must be replayable
- extra storage needed to store the events, which grows at a similar pace or higher than the current data storage
- the event storage format is another frozen API, since old events stored in the log need to be readable
- increased memory usage for docker deployments due to the extra log service
- limits on the event store (e.g. event size) are passed on to the system

It is important to highlight that snapshots can and will differ according to the handling of events.
While possible in principle to completely reproduce the original handlers for a snapshot, in practice this is hard and has diminishing returns.
Prime is the case of benign bug fixes that change the output: the result was not the same, but the previous result was incorrect.
For any given long running system using snapshots, it is likely that a fresh new system that replays all events will yield a slightly different snapshot.

