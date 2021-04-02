# Backend architecture decision doc

Status: work in progress, comments & contributions requested

## Background

Currently Athens runs as a local app, with data stored in a local file. To allow
users to conveniently use Athens across several devices, Athens will need to
share state across those devices, allowing them to read/write the database.

This doc is intended to converge on the right way to implement the first Athens
backend.

## Design goals

* Allowing one user to use a graph on multiple devices
  * Potentially: small group of trusted users on one graph
* One graph per server

## Design non-goals

* SaaS
  * We assume that the user trusts the server (i.e., runs the server for their
    graph, or trust whoever runs the server). The database does not have to be
    opaque/secret to the server. It's OK if the server can read or alter the
    database.
  * Eventual SaaS design will need to solve this and other security/privacy
    considerations.
* Massive multiplayer
  * We are not designing for more than, say, 10 users on a graph.
* Fine-grained authorization
  * An authenticated user will have read/write access to the whole database. We
    will not, for example, have ACLs.

## Requirements

### Self-hostable

The backend should not depend on closed-source software or code whose license is
incompatible with the [Eclipse Public License version 1](https://en.wikipedia.org/wiki/Eclipse_Public_License) (which Athens uses).
This is a hard requirement.

### Ease of hosting

The easier the backend is to self-host, the better. Some backends (like IPFS)
might not even require running server, which is a point in their favor.
Some backends (like [Solid](#solid)) already have general-purpose servers
which we can hook into without needing to even code an Athens-specific server.
The less code we have to maintain, the better.

### Scaling

TODO: needs to scale to O(1000) pages, no need to scale to O(1 million) pages

### Datomic-like interface

Athens currently uses [DataScript](https://github.com/tonsky/datascript) as its
database. Its philosophy is based on Datomic, which is a major Clojure project
and many Clojurists are familiar with it. Backends that provide a Datomic-like
interface get bonus points, because they don't ask existing Clojurists familiar
with Datomic to learn a new language to talk to the database.

### History

TODO: DataScript supports history; would be better to have it than not to.

### Offline editing

TODO: would be nice to have a system that does not require a client to be online
100% of the time; this would require some form of conflict resolution. if it
would be too hard to get for now, it would still be good to start with a backend
for which we have an idea of how it could eventually support offline read/write
access, even if we start out only supporting online writing.

### Bandwidth

If the system needs to download the whole graph before working, it's probably
not going to scale well to larger graphs - especially on phones. Currently,
Athens database stores all history. As a data point, [Rai][rai]'s
`index.transit` with 1 month of usage has ~800 kB. [Rai][rai] believes this is
a scaling bottleneck Roam Research has hit at some point.

In terms of saving bandwidth:

* (1) "downloading whole database on client start, then sync changes" is worse
  than
* (2) "downloading changes since last sync on client start", which is worse than
* (3) "downloading chunks of database as they are needed".

(2) would imply that the first start of each client would be slow, but
subsequent starts might be faster. The client would also need to store the whole
database locally, which might become an issue with really big databases.

Compared to (2), (3) would let clients have a faster first start, and
potentially store less data locally.

If clients can do (1), they should also be able to do (2) without too many
changes. If they can keep their state in-memory and apply updates from server on
top, the difference is just persisting that in-memory state on client shutdown
and requesting new changes since shutdown from the server on next client start.

### Decision reversibility

TODO: the less we have to change Athens to fit a particular backend, the better;
that way if we end up needing to change the backend, it can be done easier. "the
more opinionated, the less good".

### Modularity

TODO:
. would be nice to keep around option to use
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

#### Authentication

The backend needs to have some form of authentication, and only allow
authenticated users to read/write the database.

One option to implement this would be to assume that there is a trusted secure
network between the server and clients, but nowadays, it's better to assume that
[the network is hostile](https://blog.cryptographyengineering.com/2015/08/16/the-network-is-hostile/).
Corporate networks can be penetrated. If we ask users to ensure they run over
VPN, we make security their responsibility, and we invite misconfiguration and
insecurity.

So, let's implement at least a minimum form of security, like a shared secret.
For such a secret, we should ensure it's secure enough (i.e., not easily
guessable). We also need to prevent replay attacks. One way to prevent them
would be a challenge-response protocol.

OAuth might be also a good option. With OAuth, a resource server (i.e., Athens
backend) can just receive a request from the user, check against the identity
provider (e.g., Google, GitHub, etc.) that the user is authenticated, and allow
the user in if the user is on an "allowed users" list for the graph/Athens
database. Compared to the shared secret option, its security would not depend
on the user choosing a strong enough shared secret.

#### Authorization

We are not addressing the SaaS use case, and we are assuming that users want
to share their whole graph. So when a user is authenticated, there will be no
further authorization - any user that is allowed to access the backend can be
allowed to read/write any of it.

If the backend supports or is easily extensible with per-entity authorization,
it's a point in favor, because it would be helpful for eventual Athens SaaS.

#### Encryption in transit

Data needs to be encrypted in transit (e.g., by SSL, or other solutions) to
prevent eavesdropping on the connection.

#### Encryption at rest

If the backend database is publicly readable (like with IPFS), the data will
also need to be encrypted at rest. With such systems, data published once can be
potentially stored by anyone forever, so we would also need to design to make
it unlikely that a user could make their whole graph accidentally publicly
readable, like by having their password involved in a security leak.

If the backend database is stored on the server (and not publicly readable,
like with IPFS), it does not have to be encrypted at rest, because we assume a
model where the user runs their server and trusts it with their data. We are
explicitly *not* addressing the SaaS use case.

## Alternatives considered

### Datahike server

[Abhinav][abhinav] has been working on this [his Athens
fork `presence` branch](https://github.com/arkRedM/athens/tree/feat/presence),
and a private [`athens-sync` repo for the server](https://github.com/arkRedM/athens-sync).

TODO: one backend that maintains Datahike DB, and clients are connected to it
via WebSocket 100% of the time; requires full sync on first open. benefits:
Datahike has a bunch of backends including IPFS. drawbacks: needs clients to be
online for writing; could we handle offline client writes?

TODO: describe presence features (display names, etc.)

Since [Abhinav][abhinav] has already put in a lot of work towards this, it might
make sense to use it as an interim solution. Even though it has drawbacks, it
can be used by users to get some version of multi-device Athens for their graph,
while we can continue thinking about a long-term backend that patches those.

### Fluree

TODO: expand

### Solid

TODO: Rai's overall stance: Solid is not great now as backend because of no
built-in support for history/offline edits. we could serialize a database like
Datahike in Solid but it would nullify the linked data benefit of Solid because
then we'd not store the semantic structure of data in Athens, rather the DAG
structure of the DB. so just keep eye open for potential future integration.

TODO: benefit of Solid is that there's already a bunch of pod hosting services;
if backed by Solid, Athens would not have to maintain any infrastructure, just
let users run on whatever pod they want (including self-hosted)

### TODO: fill more alternatives

Rai's ideas:

* Replikativ
  * Replikativ backed by IPFS (should be possible, there's at least one demo of
    it)

## Summary table

TODO: add history support column

|                                       | [Ease of hosting](#ease-of-hosting) | [Scaling](#scaling) | [Datomic-like](#datomic-like-interface) | [Offline editing](#offline-editing) | [Bandwidth](#bandwidth)  | [Ease of hosting](#ease-of-hosting) | [Maturity](#maturity) | [Linked data compatibility](#linked-data-compatibility) |
|---------------------------------------|-------------------------------------|---------------------|-----------------------------------------|-------------------------------------|--------------------------|-------------------------------------|-----------------------|---------------------------------------------------------|
| [Datahike server](#datahike-server)   | Run public server                   | TODO                | Yes (Datahike)                          | No, must be online to edit          | Client maintains full DB | Run public server                   | TODO                  | Graph database, non-RDF                                 |


## Decision

TODO: we decided to implement it (this way) because (these reasons)

[rai]: http://agentydragon.com/about.html
[abhinav]: https://github.com/arkRedM
