# 7. Lan-Party Datascript Events

Date: 2021-06-28

## Status

Proposed.

Extends [5. Lan-Party Remoting Protocol](0005-lan-party-remoting-protocol.md)

Uses [3. Lan-Party Common Events](0003-lan-party-common-events.md)

Superceded by [9. Atomic/Composite Grapth Operations](0009-atomic-composite-grapth-operations.md)

## Context

In Lan-Party context we'll need a way to execute [3. Lan-Party Common Events](0003-lan-party-common-events.md) on Lan-Party server.

## Decision

Remote events modifying Graph structure are prefixed with `:datascript/*`.  
They should be integration tested (at least against Datahike, preferably also against Datascript).

## Consequences

We'll have canonical way of executing Graph updates and be able to execute them over the wire in Lan-Party mode.
