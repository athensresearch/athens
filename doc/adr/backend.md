# Backend architecture decision doc

Status: work in progress, comments & contributions requested

## Background

Currently Athens runs as a local app, with data stored in a local file. To allow
users to conveniently use Athens across several devices, Athens will need to
share state across those devices, allowing them to read/write the database.

This doc is intended to converge on the right way to implement the first Athens
backend.

## Design goals

* Allowing one user to use a database on multiple devices

## Design non-goals

* Multiplayer
* Hosting a backend for multiple users (i.e., "Athens as a service"); not
  included as a goal due to extra privacy/security considerations.

## Requirements

### Self-hostable

TODO: we want to continue allowing anyone to use Athens

### Scaling

TODO: needs to scale to O(1000) pages, no need to scale to O(1 million) pages

### Datascript-like interface

TODO: it's good to have because Clojurists know it

### Offline editing

TODO: would be nice to have a system that does not require a client to be online
100% of the time; this would require some form of conflict resolution. if it
would be too hard to get for now, it would still be good to start with a backend
for which we have an idea of how it could eventually support offline read/write
access, even if we start out only supporting online writing.

### Bandwidth

TODO: would be nice if it does not require you to download whole db on each
open; Roam is kind of slowed down by this, Rai believes.

### Decision reversibility

TODO: the less we have to change Athens to fit a particular backend, the better;
that way if we end up needing to change the backend, it can be done easier. "the
more opinionated, the less good".

### Modularity

TODO: it's good to keep backend options open; would be good to make it easy to
switch backend or storage if needed. would be nice to keep around option to use
local storage, especially if it takes a bunch of time to get the backend to be
stable & non-buggy.

### Ease of hosting

TODO: systems like IPFS might allow us to run without any kind of dedicated
server, or with just a very lightweight server. or if the backend already
provides a server that we could just tell people to run, without writing an
Athens-specific server, it's also good, because less maintenance cost for us.

### Maturity

TODO: better to use something with lots of documentation & knowledge, and with a
community that actively supports it and maintains it, than something really
shiny that might break or has a very low bus factor

### Linked data compatibility

TODO: this is something Rai and Matei would be interested in having. if Athens
data is shaped like RDF, it could fit nicely into the linked data ecosystem,
like Solid etc.; however this something very few databases could satisfy, and it
is not a high-priority requirement, because it could be accomplished by writing
an input/output wrapper layer on top of whatever we end up writing. TODO:
link/embed Rai's Solid writeup, link to solid-rest.

TODO: mention a few things for why linked data in Athens would be nice, like
"this page is this person", "this is a task that's also present here in Google
Task", "find all people on which my tasks are blocked" etc.

### Security

TODO: data needs to be encrypted in transit; because we're not trying yet to
have a backend shared between users, it's OK to not encrypt the data at rest and
just tell the user to take care. if we use a technology like IPFS, we'll also
need encryption at rest.

## Alternatives considered

### Datahike server

TODO: basically abhinav@'s current WIP. one backend that maintains Datahike DB,
and clients are connected to it via WebSocket 100% of the time; requires full
sync on first open. benefits: Datahike has a bunch of backends including IPFS.
drawbacks: needs clients to be online for writing; could we handle offline client
writes?

### Fluree

TODO: expand

### Solid

TODO: Rai's overall stance: Solid is not great now as backend because of no
built-in support for history/offline edits. we could serialize a database like
Datahike in Solid but it would nullify the linked data benefit of Solid because
then we'd not store the semantic structure of data in Athens, rather the DAG
structure of the DB. so just keep eye open for potential future integration.

### TODO: fill more alternatives

TODO: create overall table of alternatives considered X criteria

Rai's ideas:

* Replikativ
  * Replikativ backed by IPFS (should be possible, there's at least one demo of
    it)
