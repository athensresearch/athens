# 14. Atomic Graph Operations transacting

Date: 2021-10-20

## Status

Proposed.

Amends [10. Atomic/Composite Grapth Operations](0010-atomic-composite-grapth-operations.md)

## Context

Some Atomic Graph Operations can't be executed within context of 1 transaction.

Some examples:
* 2x `:block/remove` leaves a gap in `:block/order` (unless removes where at the end of children list).
* 2x `:block/new` where 2nd operation provides relative position referencing block created by 1st operation.

This issues showed up while working on `:block/remove`, which seems to be easiest of this problem class,  
because it generate gaps in `:block/order`, and we could delegate order cleanup to `order-keeper`.

## Decision

To facilitate multiple atomic operations execution we'll have to break up composite operations into  
list of atomic ops, then resolve & transact each atomic op separately.

## Consequences

This approach of transacting each Atomic operation in isolation is introducing a lot of overhead.  
Clearly it's not ideal, but our current data structures don't allow us for smarter approach.

We'll have to revisit this decision when we are ready to take on CRDTs or other smarter structures.
