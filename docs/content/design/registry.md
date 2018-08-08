---
title: "Registry"
date: 2018-02-11T15:58:56-05:00
---

## The Athens Registry

The Athens registry is a Go package registry service that is hosted globally across multiple cloud providers. The **global deployment** will have a DNS name (i.e. `registry.golang.org`) that round-robins across each **cloud deployment**. We will use the following **cloud deployments** for _example only_ in this document:

- Microsoft Azure (hosted at `microsoft.registry.golang.org`)
- Google Cloud (hosted at `google.registry.golang.org`)
- Amazon AWS (hosted at `amazon.registry.golang.org`)

Regardless of which **cloud deployment** is routed to, the **global deployment** must provide up-to-date (precise definition below) module metadata & code.

We intend to create a foundation (the TBD foundation) that manages **global deployment** logistics and governs how each **cloud deployment** participates.

## Glossary

In this document, we will use the following keywords and symbols:

- `OA` - the registry **cloud deployment** hosted on Amazon AWS
- `OG` - the registry **cloud deployment** hosted on Google Cloud
- `OM` - the registry **cloud deployment** hosted on Microsoft Azure
- `MxVy` - the module `x` at version `y`

## Properties of the Registry

The registry should obey the following invariants:

- No existing module or version should ever be deleted or modified
    - Except for exceptional cases, like a DMCA takedown (more below)
- Module metadata & code may be eventually consistent across **cloud deployments**

These properties are both important to design the **global deployment** and to ensure repeatable builds in the Go community as much as is possible.

## Technical Challenges

A registry **cloud deployment** has two major concerns:

- Sharing module metadata & code
- Staying current with what other registry **cloud deployment**s are available

For the rest of this document, we’ll refer to these concerns as **data exchange** and **membership**, respectively.

Registries will use separate protocols to do **data exchange** and **membership**.

## Data Exchange

The overall design of the **global deployment** should ensure the following:

- Module metadata and code is fetched from the appropriate source (i.e. a VCS)
- Module metadata and code is replicated across all **cloud deployment**s. As previously stated, replication may be eventually consistent.

Each **cloud deployment** holds:

- A module metadata database
- A log of actions it has taken on the database (used to version the module database)
- Actual module source code and metadata
    - This is what vgo requests
    - Likely stored in a CDN

The module database holds metadata and code for all modules that the cloud deployment is aware of, and the log records all the operations the cloud deployment has done in its lifetime.

## The Module Database

The module database is made up of two components:

- A blob storage system (usually a CDN) that holds module metadata and source code
    - This is called the module CDN
- A key/value store that indicates whether and where a module MxV1 exists in the **cloud deployment**'s blob storage
    - This is called the module metadata database, or key/value storage

If a **cloud deployment** OM holds modules `MxV1`, `MxV2` and `MyV1`, its module metadata database would look like the following:

```
Mx: {baseLocation: mycdn.com/Mx}
My: {baseLocation: mycdn.com/My}
```

Note that `baseLocation` is intended for use in the `<meta>` redirect response passed to vgo. As a result, it may point to other **cloud deployment** blob storage systems. More information on that in the synching sections below.

## The Log

The log is an append-only record of actions that a **cloud deployment** OM has taken on its module database. The log exists only to facilitate module replication between **cloud deployment**s (more on how replication below).

Below is an example event log:

```
ADD MxV1 ID1
ADD MxV2 ID2
ADD MyV1.5 ID3
```

This log corresponds to a database that looks like the following:

```
Mx: {baseLocation: mycdn.com/Mx}
My: {baseLocation: mycdn.com/My}
```

And blob storage that holds versions 1 and 2 of Mx and version 1.5 of My.

### Log IDs

Note that each event log line holds ID data (`ID1`, `ID2`, etc...). These IDs are used to by other **cloud deployment**s as database versions. Details on how these IDs are used are below in the pull sync section.

## Cache Misses

If an individual **cloud deployment** OM gets a request for a module MxV1 that is not in its database, it returns a "not found" (i.e. HTTP 404) response to vgo. Then, the following happens:

- OM starts a background cache fill operation to look for MxV1 on OA and OG
    - If OA and OG both report a miss, OM does a cache fill operation from the VCS and does a push synchronization (see below)
- vgo downloads code directly from the VCS on the client's machine

## Pull Sync

Each **cloud deployment** will actively sync its database with the others. Every timer tick `T a **cloud deployment** OM will query another **cloud deployment** OA for all the modules that changed or were added since the last time OM synched with OA.

### Query Mechanism

The query obviously relies on OA being able to provide deltas of its database over logical time. Logical time is communicated between OM and OA with log IDs (described above). The query algorithm is approximately:

```
lastID := getLastQueriedID(OA)
newDB, newID := query(OA, lastID) // get the new operations that happened on OA's database since lastID
mergeDB(newDB) // merge newDB into my own DB
storeLastQueriedID(OA, newID) // after this, getLastQueriedID(OA) will return newID
```

The two most important parts of this algorithm are the `newDB` response and the `mergeDB` function.

#### Database Diffs

OA uses its database log to construct a database diff starting from the `lastID` value that it receives from OM. It then sends the diff to OM in JSON that looks like the following:

```json
{
	"added": ["MxV1", "MxV2", "MyV1"],
	"deleted": ["MaB1", "MbV2"],
	"deprecated": ["MdB1"]
}
```

Explicitly, this structure indicates that:

- `MxV1`, `MxV2` and `MyV1` were added since `lastID`
- `MaB1` and `MbV2` were deleted since `lastID`
- `MdB1` was deprecated since `lastID`

#### Database Merging

The `mergeDB` algorithm above receives a database diff and merges the new entries into its own database. It follows a few rules:

- Deletes insert a tombstone into the database
- If a module `MdV1` is tombstoned, all future operations that come via database diffs are sent to `/dev/null`
- If module `MdV2` is deprecated, future add or deprecation diffs for `MdV2` are sent to `/dev/null`. Future delete operations can still tombstone

The approximate algorithm for `mergeDB` is this:

```
func mergeDB(newDB) {
	for added in newDB.added {
		fromDB := lookup(added)
		if fromDB != nil {
			break // the module already exists (it may be deprecated or tombstoned), bail out
		}
		addToDB(added) // this adds the module to the module db's key/value store, but points baseLocation to the other cloud deployment's blob storage
		go downloadCode(added) // this downloads the module to local blob storage, then updates the key/value store's baseLocation accordingly
	}
	for deprecated in newDB.deprecated {
		fromDB := lookup(deprecated)
		if fromDB.deleted() {
			break // can't deprecated something that's already deleted
		}
		deprecateInDB(deprecated) // importantly, this function inserts a deprecation record into the DB even if the module wasn't already present!
	}
	for deleted in newDB.deleted {
		deleteInDB(deleted) // importantly, this function inserts a tombstone into the DB even if the module wasn't already present!
	}
}		
			
```

## Push Sync

If a **cloud deployment** OM has a cache miss on a module MxV1, does a cache fill operation and discovers that no other **cloud deployment** OG or OA have MxV1, it fills from the VCS. After it finishes the fill operation, it saves the module code and metadata to its module database and adds a log entry for it. The algorithm look like the following:

```
newCode := fillFromVCS(MxV1)
storeInDB(newCode)
storeInLog(newCode)
pushTo(OA, newCode) // retry and give up after N failures
pushTo(OG, newCode) // retry and give up after N failures
```

The `pushTo` function is most important in this algorithm. It _only_ sends the existence of a new module, but no event log metadata (i.e. `lastID`):

```
func pushTo(OA, newCode) {
	http.POST(OA, newCode.moduleName, newCode.moduleVersion, "https://OM.com/fetch")
}
```

The endpoint in OA that receives the HTTP `POST` request in turn does the following:

```
func receive(moduleName, moduleVersion, fetchURL) {
	addToDB(moduleName, moduleVersion, OM) // stores moduleName and moduleVersion in the key/value store, with baseLocation pointing to OM
	go downloadCode(added) // this downloads the module to local blob storage, then updates the key/value store's baseLocation accordingly
```

Note again that `lastID` is not sent. Future pull syncs that OA does from OM will receive moduleName/moduleVersion in the 'added' section, and OA will properly do nothing because it already has moduleName/moduleVersion.