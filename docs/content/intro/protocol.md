---
title: "Download protocol"
date: 2018-02-11T16:58:56-05:00
---

Athens builds on top of Go CLI which specifies a set of endpoints with which it communicates with external proxies providing modules. This set of endpoints we call _Download Protocol_

The original vgo research paper on Download protocol can be found here: https://research.swtch.com/vgo-module

Each of these endpoints sits on top of a module. Let's assume module `htp` authored by `acidburn`.

So for each of endpoints mentioned bellow we will assume address `acidburn/htp/@v/{endpoint}` (e.g `acidburn/htp/@v/list`)

## List of versions

This endpoint returns a list of versions that Athens knows about for `acidburn/htp`. The list is just separated by newlines:

```HTTP
GET athens.io/acidburn/htp/@v/list
```

```HTML
v1.0
v1.1
v2.0
```

## Version info


```HTTP
GET athens.io/acidburn/htp/@v/v1.0.0.info
```

This returns JSON with information about v1.0.0. It looks like this:

```json
{
    "Name": "v1.0.0",
    "Short": "v1.0.0",
    "Version": "v1.0.0",
    "Time": "1972-07-18T12:34:56Z"
}
```

## Go.mod file

```HTTP
GET athens.io/acidburn/htp/@v/v1.0.0.mod
```

This returns the go.mod file for version v1.0.0. If athens.io/acidburn/htp version `v1.0.0` has no dependencies, the response body would look like this:

```
module "athens.io/acidburn/htp"
```

## Module sources

```HTTP
GET athens.io/acidburn/htp/@v/v1.0.0.zip
```

This is what it sounds like — it sends back a zip file with the source code for the module in version v1.0.0.

## Latest

```HTTP
GET athens.io/acidburn/htp/@latest
```

This endpoint returns the latest version of the module.
If the version does not exist it should retrieve the hash of latest commit.
