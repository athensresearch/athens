---
title: Walkthrough
description: Understanding the Athens proxy and Go Modules
menu: shortcuts
---

First, make sure you have [Go 1.11 installed](https://gophersource.com/setup/) and that GOPATH/bin is on your path.

## Without the proxy
Let's review what everything looks like in Go 1.11 without the proxy in the picture:

**Bash**
```console
$ git clone https://github.com/athens-artifacts/walkthrough.git
$ cd walkthrough
$ GO111MODULE=on go run .
go: downloading github.com/athens-artifacts/samplelib v1.0.0
The 🦁 says rawr!
```

**PowerShell**
```console
$ git clone https://github.com/athens-artifacts/walkthrough.git
$ cd walkthrough
$ cmd /C "set GOMODULE111=on && C:\GO\bin\go run ."
go: downloading github.com/athens-artifacts/samplelib v1.0.0
The 🦁 says rawr!
```

The end result of running this command is that Go downloaded the package source and packaged
it into a module, saving it in the Go Modules cache.

Now that we have seen Go Modules in action without the proxy, let's take a look at
how the proxy changes the workflow and the output.

## With the proxy
Using the most simple installation possible, let's walk through how to use the
Athens proxy, and figure out what is happening at each step.

Note: [Currently, the proxy does not work on Windows](https://github.com/gomods/athens/issues/532).

Before moving on, let's clear our Go Modules cache so that we can see the proxy
in action without any caches populated:

**Bash**
```bash
sudo rm -fr $(go env GOPATH)/pkg/mod
```

<!-- 
**PowerShell**
```powershell
rm -recurse -force $(go env GOPATH)\pkg\mod
```
-->

Now run the Athens proxy in a background process:

**Bash**
```console
$ cd ..
$ git clone https://github.com/gomods/athens.git
$ cd athens
$ GO111Modules=off go run ./cmd/proxy &
[1] 25243
INFO[0000] Starting application at 127.0.0.1:3000
```

Note: [Building Athens Go Modules enabled is not yet supported](https://github.com/gomods/athens/pull/371), so we have turned it off in the above example.

<!--
**PowerShell**
```console
$ cd ..
$ git clone https://github.com/gomods/athens.git
$ cd athens
$ start -NoNewWindow go "run .\cmd\proxy"
[1] 25243
INFO[0000] Starting application at 127.0.0.1:3000
```
-->

The Athens proxy is now running in the background and is listening for requests
from localhost (127.0.0.1) on port 3000.

Since we didn't provide any specific configuration
the proxy is using in-memory storage, which is only suitable for trying out the proxy
for a short period of time, as you will quickly run out of memory and the storage
doesn't persist between restarts.

Next, you will need to enable the [Go Modules](https://github.com/golang/go/wiki/Modules)
feature and configure Go to use the proxy!

**Bash**
```bash
export GO111MODULE=on
export GOPROXY=http://127.0.0.1:3000
```

<!--
**PowerShell**
```powershell
$env:GO111MODULE = "on"
$env:GOPROXY = "http://127.0.0.1:3000"
```
-->

The `GO111MODULE` environment variable controls the Go Modules feature in Go 1.11 only.
Possible values are:

* `on`: Always use Go Modules
* `auto` (default): Only use Go Modules when a go.mod file is present, or the go command is run from _outside_ the GOPATH
* `off`: Never use Go Modules

The `GOPROXY` environment variable tells the `go` binary that instead of talking to
the version control system, such as github.com, directly when resolving your package
dependencies, instead it should communicate with a proxy. The proxy implements
the [Go Download Protocol](/intro/protocol), and is responsible for listing available
versions for a package in addition to providing a zip of particular package versions.

Now, you when you build and run this example application, `go` will fetch dependencies via Athens!

```console
$ cd ../walkthrough
$ go run .
go: finding github.com/athens-artifacts/samplelib v1.0.0
handler: GET /github.com/athens-artifacts/samplelib/@v/v1.0.0.info [200]
handler: GET /github.com/athens-artifacts/samplelib/@v/v1.0.0.mod [200]
go: downloading github.com/athens-artifacts/samplelib v1.0.0
handler: GET /github.com/athens-artifacts/samplelib/@v/v1.0.0.zip [200]
The 🦁 says rawr!
```

The output from `go run .` includes attempts to find the **github.com/athens-artifacts/samplelib** dependency. Since the
proxy was run in the background, you should also see output from Athens indicating that it is handling requests for the dependency.

Let's break down what is happening here:

1. Before Go runs our code, it detects that our code depends on the **github.com/athens-artifacts/samplelib** package
   which is not present in the vendor directory.
1. At this point the Go Modules feature comes into play because we have it enabled.
    Instead of looking in the GOPATH for the package, Go reads our **go.mod** file
    and sees that we want a particular version of that package, v1.0.0.

    ```go
    module github.com/athens-artifacts/walkthrough
    
    require github.com/athens-artifacts/samplelib v1.0.0
    ```
1. Go first checks for **github.com/athens-artifacts/samplelib@v1.0.0** in the Go Modules cache,
    located in GOPATH/pkg/mod. If that version of the package is already cached,
    then Go will use it and stop looking. But since this is our first time
    running this, our cache is empty and Go keeps looking.
1. Go requests **github.com/athens-artifacts/samplelib@v1.0.0** from our proxy because
    it is set in the GOPROXY environment variable.
1. The proxy checks its own storage (in this case is in-memory) for the package and doesn't find it. So it
    retrieves it from github.com and then saves it for subsequent requests.
1. Go downloads the module zip and puts it in the Go Modules cache
    GOPATH/pkg/mod.
1. Go will use the module and build our application!

Subsequent calls to `go run .` will be much less verbose:

```
$ go run .
The 🦁 says rawr!
```

No additional output is printed because Go found **github.com/athens-artifacts/samplelib@v1.0.0** in the Go Module
cache and did not need to request it from the proxy.

## Next Steps

Now that you have seen Athens in Action:

* Learn more how to share an Athens server with your development team. [Coming Soon/Help Wanted](https://github.com/gomods/athens/issues/533)
* Explore best practices for running Athens in Production. [Coming Soon/Help Wanted](https://github.com/gomods/athens/issues/531)
