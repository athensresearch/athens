# vgoprox

_This is a very early alpha release, and the API will be changing as the proxy API changes._
_Do not run this in production_

vgoprox is a proxy server for [vgo modules](https://github.com/golang/go/wiki/vgo). It implements
the download protocol specified [here](https://research.swtch.com/vgo-module) 
(under "Download Protocol").

# Storage

This server can be approximately split into the API surface and the backing storage. The API
surface is specified in the vgo modules paper (see above), and the backing storage approximately 
reflects the API surface -- in most cases, versions are stored "under" their modules, and 
modules are stored "under" their base paths.

Currently, there is only an in-memory storage driver. That means whenever the server dies,
all module metadata (version, name, `go.mod` files) and module source is deleted.

There are a few more storage modules on deck:

- On local disk
- MySQL Database
- Various cloud blog stores

# CLI

In addition to the standard vgo API, there is also a crude "admin" API that allows you to upload
new versions of modules to the server. The API is crude mostly because it has no concept of
authentication or authorization. Everybody has "god mode"!

There's a very basic CLI that makes it easy(ish) to upload a new module to the server. Find the
code for it in the [./cli](./cli) directory, and build it with the following:

```console
make cli
```

You'll get a `vgp` (short for "vgo proxy") binary in the same directory. The binary is limited
right now. Run it like this:

```console
./vgp <directory> <baseURL> <module> <version>
```

A few additional notes on this CLI:

- It is hard coded to make requests against `http://localhost:3000`, so you'll need to have the 
`vgoprox` server running to successfully use it (see [Development](#development) above)
- `<directory>` will be zipped up and uploaded to the vgoprox server
- ... and it needs to have a `go.mod` file in its root
- The go.mod file's 'module' directive must match `<module>`. `vgp` won't read that 
value yet (that's planned though)
- If there are any `vendor` directories under `<directory>`, they won't be ignored yet, but that's
planned

# Does it Work?

Great question (especially for an alpha project)! The short answer is this:

>The basic pieces are in place, but the CLI and the server makes it near-impossible to 
use this thing in the real world

And here are some details:

The basic API and storage system work, but the proxy is limited for real world use right now.

First, it only stores modules in memory, so that's a major issue if you want to use it for anything
real.

Second, it doesn't hold any packages other than the ones you upload to it. A package proxy
is pretty much only as useful as the packages it stores. You can work around that by declaring
dependencies as `file:///` URLs if you want, but that defeats much of the purpose of this project.

When vgoprox has better storage drivers (at least persistent ones!), it'll be easier to load it up 
with modules (i.e. by running a background job to crawl your `GOPATH`). At that point, it'll be
more practical to successfully run `vgo get` inside a less-trivial project.

Finally, here's what the whole workflow looks like in the real world (spoiler alert: the CLI needs
work). The setup:

- First, I uploaded a basic module to the server using the CLI (see above) using the following command 
from the root of this repo:
```console
./vgp ./testmodule arschles.com testmodule v1.0.0
```
- Then I created a new module with the following files in it:
    - A single `go.mod` file with only the following line in it: `module "foo.bar/baz"`
    - A `main.go` file with the following in it:
    ```go
    package main
    func main() {}
    ```

Finally, from the root of the new module, I ran `vgo get arschles.com/testmodule@v1.0.0` and got the 
following output:

```console
$ vgo get arschles.com/testmodule@v1.0.0
vgo: downloading arschles.com/testmodule v1.0.0
vgo: import "arschles.com/testmodule": zip for arschles.com/testmodule@v1.0. has unexpected file testmodule/.DS_Store
```

As you can see, the CLI uploaded a file to vgoprox that's not `.go`, `go.mod`, or anything else 
that `vgo` allows, so at least the CLI needs some work (and the server needs some sanity checks too).

You can get around all of this by manually zipping up your code and uploading it with `curl` or 
similar, but like I said, that's super impractical. Yay alpha software!

# Development

The server is written using [Buffalo](https://gobuffalo.io/), so it's fairly straightforward
to get started on development. Download 
[v0.10.3](https://github.com/gobuffalo/buffalo/releases/tag/v0.10.3) or later, untar/unzip the
binary into your PATH, and then run the following from the root of this repository:

```console
buffalo dev
```

You'll see some output in your console that looks like this:

```console
Aarons-MacBook-Pro:vgoprox arschles$ buffalo dev
buffalo: 2018/02/25 16:09:36 === Rebuild on: :start: ===
buffalo: 2018/02/25 16:09:36 === Running: go build -v -i -o tmp/vgoprox-build  (PID: 94067) ===
buffalo: 2018/02/25 16:09:37 === Building Completed (PID: 94067) (Time: 1.115613079s) ===
buffalo: 2018/02/25 16:09:37 === Running: tmp/vgoprox-build (PID: 94078) ===
time="2018-02-25T16:09:37-08:00" level=info msg="Starting application at 127.0.0.1:3000"
INFO[2018-02-25T16:09:37-08:00] Starting Simple Background Worker

Webpack is watching the files…
```

After the `Starting application at 127.0.0.1:3000` is logged, the server is up and running.
As you edit and save code, Buffalo will automatically restart the server. **This means that
all of your modules will disappear** because the only storage driver is in-memory right now.

See [CLI](#CLI) for information on how to add modules back into the server.

# Resources:

- ["Go and Versioning"](https://research.swtch.com/vgo) papers
- [vgo wiki](https://github.com/golang/go/wiki/vgo)
