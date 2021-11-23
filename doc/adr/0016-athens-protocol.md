# 15. Athens Protocol

Date: 2021-11-23


## Status

Accepted.


## Context

The Athens Protocol defines the format and semantics for messages between Athens nodes.
These nodes can be standalone clients, clients connected to servers, or other future architectures.

The protocol aims to support a few key requirements:
  1. longevity: information must be usable for at least a long period of time in the future, e.g. 10+ years
  2. robustness: nodes must be able to synchronize through unreliable network connections
  3. reliability: data must not be lost and remain acessible
  4. extensibility: the protocol must support extension over time


## Decision

We decided to model the Athens Protocol as an append-only log of deterministic operations.
Operations from multiple nodes are weaved together into a single canonical log.
Knowledge graph state is determined by the reduction of all operations.
The current state can be represented by a canonical view.

Operations express semantic changes to the knowledge graph.
Non-trivial operations are expressed as composites of atomic operations.
The effect of atomic operations is kept small and local to their direct relationships.
Future versions of the protocol can enhance the vocabulary of atomic operations, but can never remove existing ones.

Client nodes synchronize state by sending optimistic operations, and listening to the canonical stream of operations from the server.
Client nodes can create a new optimistic state by applying optimistic operations over the last known state.
Synchronization can happen as frequently or infrequently as desired.
Each operation a node creates has a unique id used for identification and to prevent duplication in the log.

Standalone clients do not need to synchronize with other nodes, but still express changes in the same format.
They are, essentially, a client node that never synchronizes with other nodes.

Different nodes can arrive at different states based on their interpretation of the operations.
This way nodes can select for information that is relevant to them.
This also allows for different node capabilities and conflict resolution strategies.

The initial set of atomic operations expresses only a tree structure via page identity and block identity, content and location.
For now, content parsing for references is not part of the protocol itself, and is instead left to the nodes.


## Consequences

The canonical log requires nodes to achieve consensus on what the order of operations is.

The latest graph state can only be obtained by replaying the full log, or via querying for a node-compatible view of the current state.

