---
title: "Intro"
date: 2018-02-11T16:52:23-05:00
---

![Athens logo](/banner.png)

## Welcome to Athens

Athens is the name of the combined project that includes a global registry for Go modules and a stand-alone proxy server that can be deployed on-premise to cache and control available Go modules for your organization.

## Try out Athens

To quickly see Athens in action, follow these steps:

First, make sure you have [Go 1.11 installed](https://gophersource.com/setup/) and that GOPATH/bin is on your path.

Next, use Go to install and run the Athens proxy in a background process

```console
$ go get -u github.com/gomods/athens/cmd/proxy
# the source is downloaded to GOPATH/src/github.com/gomods/athens/
$ proxy &
[1] 25243
INFO[0000] Starting application at 127.0.0.1:3000
```

Next, you will need to enable the Go Modules feature and configure Go to use the proxy!

**Bash**
```bash
export GO111MODULE=on
export GOPROXY=http://127.0.0.1:3000
```

**PowerShell**
```powershell
$env:GO111MODULE = "on"
$env:GOPROXY = "http://127.0.0.1:3000"
```


Now, you when you build and run this example application, **go** will fetch dependencies via Athens!

```console
$ git clone https://github.com/athens-artifacts/happy-path.git
$ cd happy-path
$ go run .
go: finding github.com/athens-artifacts/no-tags v0.0.0-20180803171426-1a540c5d67ab
handler: GET /github.com/athens-artifacts/no-tags/@v/v0.0.0-20180803171426-1a540c5d67ab.info [200]
handler: GET /github.com/athens-artifacts/no-tags/@v/v0.0.0-20180803171426-1a540c5d67ab.mod [200]
go: downloading github.com/athens-artifacts/no-tags v0.0.0-20180803171426-1a540c5d67ab
handler: GET /github.com/athens-artifacts/no-tags/@v/v0.0.0-20180803171426-1a540c5d67ab.zip [200]
```

The output from **go run .** includes attempts to find the **github.com/athens-artifacts/no-tags** dependency. Since the proxy was run in the background, you also 
see output from Athens indicating that it is handling requests for the dependency.

This should give you an overview of what using Athens is like. For more detailed information on how to run Athens in production, check out our other documentation (coming soon!!).

---
Athens banner attributed to Golda Manuel
