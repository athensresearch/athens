# 6. Lan-Party Presence Events

Date: 2021-06-28

## Status

Proposed

Extends [5. Lan-Party Remoting Protocol](0005-lan-party-remoting-protocol.md)

## Context

In Lan-Party we'll need to communicate other Athenians presence in current Knowledge Graph.

## Decision

We want to utilize existing [5. Lan-Party Remoting Protocol](0005-lan-party-remoting-protocol.md).

We'll need to communicate at least:
- Someone going *Online* & *Offline*
- Viewing page uid
- Editing block uid

## Consequences

Some UI behaviors should be influenced by presence state, like when someone is already editing block, nobody else should be able to edit from beneath them.

