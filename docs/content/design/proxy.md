---
title: "Proxy"
date: 2018-02-11T15:59:56-05:00
---

## The Athens Proxy

The Athens project has two components, the [central registry](./REGISTRY.md) and edge proxies.
This document details the latter.

## The Role of the Proxy

We intend proxies to be deployed primarily inside of enterprises to:

- Host private modules
- Exclude access to public modules
- Cache public modules

Importantly, a proxy is not intended to be a complete _mirror_ of an upstream registry. For public modules, its role is to cache and provide access control.

## Proxy Details

First and foremost, a proxy exposes the same vgo download protocol as the registry. Since it doesn't have the multi-cloud requirements as the registry does, it supports simpler backend data storage mechanisms. We plan to release a proxy with several backends including:

- In-memory
- Disk
- RDBMS
- Cloud blob storage

Users who want to target a proxy configure their `vgo` CLI to point to the proxy, and then execute commands as normal.

## Cache Misses

When a user requests a module `MxV1` from a proxy and the proxy doesn't have `MxV1` in its cache, it first determines whether `MxV1` is private or not private.

If it's private, it immediately does a cache fill operation from the internal VCS.

If it's not private, the proxy consults its exclude list for non-private modules (see below). If `MxV1` is on the exclude list, the proxy returns 404 and does nothing else. If `MxV1` is not on the exclude list, the proxy executes the following algorithm:

```
registryDetails := lookupOnRegistry(MxV1)
if registryDetails == nil {
	return 404 // if the registry doesn't have the thing, just bail out
}
return registryDetails.baseURL
```

The important part of this algorithm is `lookupOnRegistry`. That function queries an endpoint on the registry that either:

- Returns 404 if it has `MxV1` in the registry
- Returns the base URL for MxV1 if it has `MxV1` in the registry

Finally, if `MxV1` is fetched from a registry server, a background job will be created to periodically check `MxV1` for deletions and/or deprecations. In the event that one happens, the proxy will delete it from the cache.

_In a later version of the project, we may implement an event stream on the registry that the proxy can subscribe to and listen for deletions/deprecations on modules that it cares about_

## Exclude Lists and Private Module Filters

To accommodate private (i.e. enterprise) deployments, the proxy maintains two important access control mechanisms:

- Private module filters
- Exclude lists for public modules

### Private Module Filters

Private module filters are string globs that tell the proxy what is a private module. For example, the string `github.internal.com/**` tells the proxy:

- To never make requests to the public internet (i.e. to the registry) regarding this module
- To download module code (in its cache filling mechanism) from the VCS at `github.internal.com`

### Exclude Lists for Public Modules

Exclude lists for public modules are also globs that tell the proxy what modules it should never download from the registry. For example, the string `github.com/arschles/**` tells the proxy to always return `404 Not Found` to clients.