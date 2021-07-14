# 5. Lan-Party Remoting Protocol

Date: 2021-06-28

## Status

Proposed.

Extended by [6. Lan-Party Presence Events](0006-lan-party-presence-events.md)

Extended by [7. Lan-Party Datascript Events](0007-lan-party-datascript-events.md)

## Context

With introduction of Lan-Party we're facing inherent complexities of networking,  
something that wasn't an issue for us in Single-Player mode.

We want to have consistent, reusable, extensible communication protocol.

## Decision

*Remoting Protocol* needs to address following concerns:

- Events can be initiated from any side (client or server)
- Every event expects confirmation:
  - Acknowledged: Event got accepted
  - Rejected: When client is stale
  - Failed: When invalid/unsupported event was received
- Client should not issue new events, while awaiting confirmation of event

## Consequences

There is significant overhead in maintaining protocol state,  
this should be abstracted away from clients as much as possible,  
so when writing client code we don't need to be concerned with intricacies of underlying protocol.

We'll also need to provide facility for `re-frame` followup events.  
In Single-Player we simply could `dispatch` transaction event and followup "UI" events.

In new Lan-Party context, followup events should be only executed when event was *Acknowledged*.

We should check schema of events before sending and when reading, on both client & server.
