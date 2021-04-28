# 3. Self Hosted

Date: 2021-04-27

## Status

Proposed

## Context

We want to enable enterprise customers to self host Athens knowledge graph on premise.  
Uses should be able to collaborate on same graph, edit block & pages as well as see presence of other collaborators.

## Decision

We need a backend server that at least talks Datalog.

For 1st backend we've chosen Datahike, because it's closeness to Datascript that we already use.  
Another possible backend could be Crux or Datomic.

### Communication Layer

Because we're running in a very controlled environment (as in we don't have to support Samsung Internet & FB embedded browser)  
we can assume availability of modern Web APIs, so for client server communication we can rely purely on WebSockets.

### Concurrency

To avoid problems of concurrent updates, we'd like to have single writer approach.  
This means that updates to central graph can be done synchronously.

### Connecting clients

When client connects we have to get it in sync with backend.  

We have 2 options here:

1. Stream all the updates that happened since last tx-id client has. (easier to do because we'll need tx ingest on client for normal connected work)
2. Provide a snapshot of DB state. (more effective on busy DB)

We also have to authorize user.  
For now we can assume trust based system, so if you say you're "Plato" we believe you w/o verification.  
Authentication is a topic that deserves ADR of it's own.

### Updating sever

When Socrates makes a modification to connected DB (create, update, delete)  
this change has to send to connected backend to be transacted.

Changes that Socrates made should not be transacted locally, they should only be transacted on backend.

### Updating clients

Whenever transaction is executed on the backend it should be broadcasted to connected clients.  
Because of potential `:db/id` clashes we'll need to normalize transaction data for transport (datsync is a good example of it).

Client on receiving transaction data, transacts it to local database, and the change is reflected in UI.

### Presence

We also want to communicate to Socrates what other collaborators are present in his context.

To do that we want to broadcast presence related information via same WebSocket we have for DB.

Presence event types:
* connect/disconnect
* viewing page
* editing block

### Distribution

Docker and Docker Compose are convenient for consumption.

### Persistence

Graph DB should be persisted to some form of permanent storage, best if it can store data in Docker volume so clients can easily manage their backups.

## Consequences

Currently Athens has only Datascript DB in the frontend, now that we're introducing connected (self-hosted) DB, we need to change that.

If Athens is connected to self-hosted instance only place where DB modifications should happen is [on the server](#Updating Clients).  
This means that our Frontend DB stays, and is still used for UI, but in read only mode.

So when users make an edit it has to be transacted on the backend, and in order for edit to show up in users UI is by receiving broadcast  
from the server and transacting it to local DB.

