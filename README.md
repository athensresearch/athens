# Athens

_This is a very early alpha release, and the API will be changing as the proxy API changes._
_Do not run this in production. This warning will be changed or removed as the project and the proxy API changes._

Athens is a proxy server for [vgo modules](https://github.com/golang/go/wiki/vgo). It implements
the download protocol specified [here](https://research.swtch.com/vgo-module)
(under "Download Protocol"), and a few additional API endpoints to make it more useful. See
[API.md](./API.md) for more information.

# Architecture

Athens is composed roughly of three logical pieces. The below list contains links
to a description of each:

* [Module proxy](./PROXY.md)
* [Module registry](./REGISTRY.md)
* [CLI](./CLI.md)

# Development

The server is written using [Buffalo](https://gobuffalo.io/), so it's fairly straightforward
to get started on development. You'll need Buffalo v0.11.0 or later to do devlopment on Athens.

Download
[v0.11.0](https://github.com/gobuffalo/buffalo/releases/tag/v0.11.0) or later, untar/unzip the
binary into your PATH, and then run the following from the root of this repository:

```console
buffalo dev
```

You'll see some output in your console that looks like this:

```console
$ buffalo dev
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

## Dependencies

To run the development server, or run tests (tip: run `make test` to easily
run tests), you'll need a running MongoDB server. The easiest way to get
one is using Docker. Simply run this command and you'll have the server running:

```console
docker run -d -p 27017:27017 mongo:3.0.15-wheezy
```

# Contributing

This project is early and there's plenty of interesting and challenging work to do.

If you find a bug or want to fix a bug, I :heart: PRs and issues!

Below is a list of general areas that I'm planning to work on, so if you'd like to help there ping me or file an issue (I am 'arschles' on the Gophers Slack):

* New storage backends (probably disk next because it's the easiest on the TODO list :smile:)
* Adding new commands to the CLI (I'll probably use [Cobra](https://github.com/spf13/cobra) to do it)
* Bugfixes in the CLI
* Adding tests

The only thing I ask is that you follow the
[Contributor Covenant](https://www.contributor-covenant.org/).

# Resources:

* ["Go and Versioning"](https://research.swtch.com/vgo) papers
* [vgo wiki](https://github.com/golang/go/wiki/vgo)

# Does it Work?

Great question (especially for an alpha project)! The short answer is this:

> The basic pieces are in place for a proxy, but the CLI and the server makes
> it near-impossible to use this thing in the real world

## Some Additional Details

The basic API and storage system work, but the proxy is limited for real world use right now.

First, it only stores modules in memory, so that's a major issue if you want to use it for anything
real.

Second, it doesn't hold any packages other than the ones you upload to it. A package proxy
is pretty much only as useful as the packages it stores. You can work around that by declaring
dependencies as `file:///` URLs if you want, but that defeats much of the purpose of this project.

When athens has better storage drivers (at least persistent ones!), it'll be easier to load it up
with modules (i.e. by running a background job to crawl your `GOPATH`). At that point, it'll be
more practical to successfully run `vgo get` inside a less-trivial project.

Finally, here's what the whole workflow looks like in the real world (spoiler alert: the CLI needs
work). The setup:

* First, I uploaded a basic module to the server using the CLI (see above) using the following command
  from the root of this repo:
  `console ./athens ./testmodule arschles.com testmodule v1.0.0`
* Then I created a new module with the following files in it:
  * A single `go.mod` file with only the following line in it: `module "foo.bar/baz"`
  * A `main.go` file with the following in it:

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

As you can see, the CLI uploaded a file to athens that's not `.go`, `go.mod`, or anything else
that vgo, so at least the CLI needs some work (and the server needs some sanity checks too).

You can get around all of this by manually zipping up your code and uploading it with `curl` or
similar, but like I said, that's super impractical. Yay alpha software!
