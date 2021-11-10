# 15. Addressability

Date: 2021-11-10

## Status

Accepted.


## Context

We need to address both blocks and pages during protocol operations, both for individual entities and for positional relationships between entities.

Blocks are uniquely identified by an immutable id, the block-uid.
This ID will never change once created and is idependent from the block content.

Pages are uniquely identified by their content, usually referred to as the title.
Titles are unique but mutable.
When a title changes, all references to a title also need to change.

Positional relationships are defined by one of the unique identifiers listed above together with a first/last/before/after relationship. 

Since pages can be referred to by knowing their title content, they are content-addressable.
This means you can address a page by knowing the page human readable title.
This allows adding blocks to a page, and referring to a page within a block, by knowing the page name.
It's not possible to do this with blocks, as you need to know the block uid to reference it.

Although the identifier for a page is called a title, this does not completely reflect the role as an addressible identifier.
While developing the protocol operations for pages we naturally hit a tension between the concept of title and how it is a name.
Creation and deletion operations would reference the title, but rename and move operations would reference the name.


## Decision

The title abstraction is suitable for a page but what really matters is that it is addressed uniquely by a known name.
This points to a higher level concept where more things can have such names, and things can have more than one name.

For a page, the title, content, and unique addressable name happen to be the same.
If a page can be addressed by different names, then the title and content would no longer be the same as the name.
Blocks cannot be named right now but it sounds like something we could do.

We will use the more general name instead of just title in the Athens protocol.


## Consequences

Protocol operations for position and page will refer to name instead of title.
Actually resolutions and code for the frontend and backend can still refer to titles.

We can expand the use of names via new protocol operations in the future.

