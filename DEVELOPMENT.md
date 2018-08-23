# Development Guide for Athens

Both the registry and the proxy are written using the [Buffalo](https://gobuffalo.io/) framework. We chose
this framework to make it as straightforward as possible to get your development environment up and running.

You'll need Buffalo [v0.12.4](https://github.com/gobuffalo/buffalo/releases/tag/v0.12.4) or later to get started on Athens,
so be sure to download the CLI and put it into your `PATH`.

See our [Contributing Guide](CONTRIBUTING.md) for tips on how to submit a pull request when you are ready.

# Initial Development Environment Setup
Athens relies on having a few tools installed locally. Run `make setup-dev-env` to install them.

### Go version
Athens is developed on Go1.11+.

If you have a stable version of go running, then a later version of go can be downloaded.

For example, to get `go1.11rc2`, run the following commands which won't affect your stable version
```
go get golang.org/dl/go1.11rc2
go1.11rc2 download
```

To point Athens to `go1.11rc2` or to a different version
set the following environment variable

```
GO_BINARY_PATH=go1.11rc2
or whichever binary you want to use with athens
```

### Dependencies

# Services that Athens Needs

Both the proxy and the registry rely on several services (i.e. databases, etc...) to function
properly. We use [Docker](http://docker.com/) images to configure and run those services.

If you're not familiar with Docker, that's ok. In the spirit of Buffalo, we've tried to make
it easy to get up and running:

1. [Download and install docker-compose](https://docs.docker.com/compose/install/) (docker-compose is a tool for easily starting and stopping lots of services at once)
2. Run `make dev` from the root of this repository

That's it! After the `make dev` command is done, everything will be up and running and you can move
on to the next step.

If you want to stop everything at any time, run `make down`.

Note that `make dev` only runs the minimum amount of dependencies needed for things to work. If you'd like to run all the possible dependencies run `make alldeps` or directly the services available in the `docker-compose.yml` file.

# Run the Proxy or the Registry

As you know from reading the [README](./README.md) (if you didn't read the whole thing, that's ok. Just read the
introduction), the Athens project is made up of two components:

1. [Package Registry](https://docs.gomods.io/design/registry/)
2. [Edge Proxy](https://docs.gomods.io/design/proxy/)

To run the registry:

```console
cd cmd/olympus
buffalo dev
```

To run the proxy:

```consols
cd cmd/proxy
buffalo dev
```

After either `buffalo dev` command, you'll see some console output like:

```console
Starting application at 127.0.0.1:3000
```

And you'll be up and running. As you edit and save code, the `buffalo dev` command will notice and automatically
re-compile and restart the server.

# Run unit tests

In order to run unit tests, services they depend on must be running first:

```console
make alldeps
```

and database created:

```console
buffalo db create
buffalo db migrate up
```

then you can run the unit tests:

```console
make test-unit
```
