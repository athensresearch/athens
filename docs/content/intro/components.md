---
title: "Components"
date: 2018-02-11T16:57:56-05:00
---

From a very high-level view, we recognize 4 major components of the system.

### Client 

The client is a user, powered by go binary with module support. At the moment of writing this document, it is `go1.11beta3`

### VCS 

VCS is an external source of data for Athens. Athens scans various VCSs such as `github.com` and fetches sources from there.

### Proxy - Athens

We intend proxies to be deployed primarily inside of enterprises to:

* Host private modules
* Exclude access to public modules
* Cache public modules

Importantly, a proxy is not intended to be a complete mirror of an upstream registry. For public modules, its role is to cache and provide access control.

### Registry - Olympus

The Athens registry is a Go package registry service that is hosted globally across multiple cloud providers. The global deployment will have a DNS name (i.e. registry.golang.org) that round-robins across each cloud deployment.

The role of Olympus is to provide up-to-date module metadata & code.