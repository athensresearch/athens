# Development Guide for Athens

Both the registry and the proxy are written using the [Buffalo](https://gobuffalo.io/) framework. We chose
this framework to make it as straightforward as possible to get your development environment up and running.

You'll need Buffalo [v0.11.0](https://github.com/gobuffalo/buffalo/releases/tag/v0.11.0) or later to get started on Athens,
so be sure to download the CLI and put it into your `PATH`.

# Services that Athens Needs

Both the proxy and the registry rely on several services (i.e. databases, etc...) to function
properly. We use [Docker](http://docker.com/) images to configure and run those services.

If you're not familiar with Docker, that's ok. In the spirit of Buffalo, we've tried to make
it easy to get up and running:

1. [Download and install docker-compose](https://docs.docker.com/compose/install/) (docker-compose is a tool for easily starting and stopping lots of services at once)
2. Run `make dev` from the root of this repository

That's it! After the `make dev` command is done, everything will be up and running and you can move
on to the next step.

If you want to stop everything at any time, run `make dev-teardown`.

# Run the Proxy or the Registry

As you know from reading the [README](./README.md) (if you didn't read the whole thing, that's ok. Just read the
introduction), the Athens project is made up of two components:

1. [Package Registry](https://github.com/gomods/athens/wiki/The-Central-Package-Registry-(Olympus))
2. [Edge Proxy](https://github.com/gomods/athens/wiki/Proxies-(Zeus))

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
