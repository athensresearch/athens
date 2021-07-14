# 3. Lan-Party Common Events

Date: 2021-06-28

## Status

Proposed.

Extended by [4. Lan-Party Linkmaker](0004-lan-party-linkmaker.md)

Used by [7. Lan-Party Datascript Events](0007-lan-party-datascript-events.md)

## Context

With Self-Hosted (aka Lan-Party) we'd like to have the same cannonical way of executing DB updates,
both in Single-Player and Lan-Party.

## Decision

Every user event that results in updates to Datascript DB in Single-Player mode should be ported to Common Events.

Porting to Common Events means:
1. Creating event
   - Builder fn in `common-events`
   - Schema for event in `common-events/schema`
2. Port logic to `resolver`
3. Test it.

`resolver` should take care only of structural edits of Knowledge Graph.

Maintaining linked nature of Knowledge Graph is addressed in [4. Lan-Party Linkmaker](0004-lan-party-linkmaker.md).

## Consequences

Negative consequences:
* More code to execute the same Datascript update logic.

Positive consequences:
* With relative ease we'll be executing same updates in Single-Player and Lan-Party.
* We'll have events logic tested, not like we couldn't do it now, but still.
