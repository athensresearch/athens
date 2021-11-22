# 13. Event Log

Date: 2021-10-13


## Status

Accepted.


## Context

We've moved all of our UI transaction centric events to semantic events, where we expresse the intent of the change rather that the database transaction representation.

The Athens server and client both resolve data representations from these events and apply it to their local data storage, discarding the semantic event after resolution. 
This resolution loses information, because the semantic event contains more than just the applied data, and is specific to the particular data storage solution used at the time. 

Instead of discarding events after resolution, we can store them permanently and achieve some of the benefits of [Event Driven Architectures](https://en.wikipedia.org/wiki/Event-driven_architecture), especially around CQRS and Event Sourcing.

Major benefits in our case include:

- ease of migration, between current databases and future ones
- correctness checking, between two resolution implementations
- debugging, via replaying history of changes to runtime
- self-healing of databases, by replaying events with fixed resolutions
- decoupling of event generation and consumption, allowing more sophisticated async and offline-first usecases
- storage, for ephemeral databases

## Decision

We have decided to add an event log to the Athens server, to which we record every database-affecting operation.

This event log will be implemented as an immutable append-only log in [Fluree](https://flur.ee/).

Fluree was chosen because it offers a Datalog-adjacent query/transaction format, has a matching open-source licence we can use for the server (via Docker), good scalability, and a migration path to a cloud-native implementation. 

Another relevant reason is that Fluree is a technology the team has been interested in experimenting with since it feels a good fit for the long-term Athens use case, so getting some experience with it on a very limited domain is valuable.


## Consequences

Besides enabling the scenarios described in [context](#context), there are also negative, or at least binding, consequences: 

- total time to effect changes increases, because the server needs to store the events before processing it
- the current event format becomes a frozen API that we need to support indefinitely, since all events must be replayable
- extra storage needed to store the events, which grows at a similar pace or higher than the current data storage
- the event storage format is another frozen API, since old events stored in the log need to be readable
- increased memory usage for docker deployments due to the extra log service
- limits on the event store (e.g. event size) are passed on to the system
