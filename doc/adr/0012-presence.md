# 12. Presence

Date: 2021-09-2


## Status

Proposed.


## Context

Presence shows what block users are in.
A user can see the list of users in the DB, which of them are in the same page, which block (if visible) they are on, and navigate to where another user is.


## Decision

The information we need to keep for presence is the last known location of a user.
- previous location information is not kept
- location comprises only block uid
  - page is derived from block by the client
  - this delegates the responsibility of access control to the client
- user comprises user id and display name
    
When a user enters a page, their location is the first block.
It is possible to view a page without editing any block, but this rule allows presence to do away with any page information.

Presence for a user is removed when the user disconnects from the server.

Embeds are considered to be a UI view based on a ref.
If a user is on a block inside an embed, they are in that blocks location, not in the embed.
There might be other such views in the future and we shouldn't special-case embeds for now until we have a better understanding of whether we need to.

Location for existing users is kept on the server and provided to users on connection.


## Consequences

Permissions do not need to be taken into account when broadcasting presence.
Presence does not broadcast the page identifier, which is its title, and thus does not expose information that would need to be checked against permissions.

There's no need to lookup users.
All the relevant information to display the user presence is available in the protocol.

The rules for setting block presence can change in the future, while keeping the existing information of block location.

