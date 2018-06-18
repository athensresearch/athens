_Note: this document is out of date. Updates coming soon, but until then, see the following two design docs:_
- [Design of Olympus, the central package repository](https://github.com/gomods/athens/wiki/The-Central-Package-Registry-(Olympus))
- [Design of Zeus, the edge proxy](https://github.com/gomods/athens/wiki/Proxies-(Zeus))


# Athens

*This is a very early alpha release, and the API will be changing as the proxy API changes.*
_Do not run this in production. This warning will be changed or removed as the project and the proxy API changes._

Athens is a proxy server for [vgo modules](https://github.com/golang/go/wiki/vgo). It implements the download protocol specified [here](https://research.swtch.com/vgo-module) (under "Download Protocol"), and a few additional API endpoints to make it more useful. See [API.md](./API.md) for more information.

# Architecture

Athens is composed roughly of three logical pieces. The below list contains links to a description of each:

* [Module proxy](./PROXY.md)
* [Module registry](./REGISTRY.md)
* [CLI](./CLI.md)

# Development

The server is written using [Buffalo](https://gobuffalo.io/), so it's fairly straightforward
to get started on development. You'll need Buffalo v0.11.0 or later to do development on Athens.

Download [v0.11.0](https://github.com/gobuffalo/buffalo/releases/tag/v0.11.0) or later, untar/unzip the binary into your PATH, and then run the
following to run [Olympus](https://github.com/gomods/athens/wiki/The-Central-Package-Registry-(Olympus)):

```console
cd cmd/olympus
buffalo dev
```

... and the following to run [Athens](https://github.com/gomods/athens/wiki/Proxies-(Athens):

```console
cd cmd/proxy
buffalo dev
```

After you see something like `Starting application at 127.0.0.1:3000`, the server
is up and running. As you edit and save code, Buffalo will automatically restart the server.

## Dependencies and Set-up

To run the development server, or run tests (tip: run `make test` to easily run tests), you'll need a running MongoDB server. We plan to add more service dependencies in the future, so we are using [Docker](https://www.docker.com/) and [Docker Compose](https://docs.docker.com/compose/) to create and destroy
development environments.

To create, run the following from the repository root:

```console
docker-compose -p athens up -d
```

To destroy:

```console
docker-compose -p athens down
```

A few environment variables are expected by the application and tests. They are 
stored in `cmd/olympus/.env` and `cmd/proxy/.env`. Below is a table of the
default values:

|Variable |Value  |
|---|---:|
|POP_PATH |$PWD/cmd/proxy |
|GO_ENV |test_postgres  |
|MINIO_ACCESS_KEY |minio |
|MINIO_SECRET_KEY |minio123 |
|ATHENS_MONGO_STORAGE_URL |mongodb://127.0.0.1:27017  |

To set in bash/zsh/osx: `export POP_PATH=$PWD/cmd/proxy`
To set in fish: `set -Ux POP_PATH $PWD/cmd/proxy`

Lastly you will need to create and initialize the database.

```console
buffalo db create
buffalo db migrate up
```

# Contributing

This project is early and there's plenty of interesting and challenging work to do.

If you find a bug or want to fix a bug, I :heart: PRs and issues! If you see an issue
in the [queue](https://github.com/gomods/athens/issues) that you'd like to work on, please just post a comment saying that you want to work on it. Something like "I want to work on this" is fine.

Finally, please follow the [Contributor Covenant](https://www.contributor-covenant.org/) in everything you do on this project - issue comments, pull requests, etc...

# Resources:

* ["Go and Versioning"](https://research.swtch.com/vgo) papers
* [vgo wiki](https://github.com/golang/go/wiki/vgo)

# Does it Work?

Great question (especially for an alpha project)! The short answer is this:

> The basic pieces are in place for a proxy, but the CLI and the server makes
> it near-impossible to use this thing in the real world

## Some Additional Details

The basic API and storage system work, but the proxy is limited for real world use right now.

First, it only stores modules in memory, so that's a major issue if you want to use it for anything real.

Second, it doesn't hold any packages other than the ones you upload to it. A package proxy is pretty much only as useful as the packages it stores. You can work around that by declaring dependencies as `file:///` URLs if you want, but that defeats much of the purpose of this project.

When athens has better storage drivers (at least persistent ones!), it'll be easier to load it up
with modules (i.e. by running a background job to crawl your `GOPATH`). At that point, it'll be more practical to successfully run `vgo get` inside a less-trivial project.

Finally, here's what the whole workflow looks like in the real world (spoiler alert: the CLI needs work). The setup:

* First, I uploaded a basic module to the server using the CLI (see above) using the following command from the root of this repo: `console ./athens ./testmodule arschles.com testmodule v1.0.0`
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

As you can see, the CLI uploaded a file to athens that's not `.go`, `go.mod`, or anything else that vgo, so at least the CLI needs some work (and the server needs some sanity checks too).

You can get around all of this by manually zipping up your code and uploading it with `curl` or similar, but like I said, that's super impractical. Yay alpha software!
