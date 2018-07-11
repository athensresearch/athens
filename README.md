# Welcome Gophers!

Welcome to the Athens project! We're building all things Go package repository in here. 

1. [Package Registry](./REGISTRY.md)
2. [Edge Proxy](./PROXY.md)

If you want more of a tl;dr on the project, check out [this quick introduction](https://medium.com/@arschles/project-athens-c80606497ce1)

# Project Status

Project Athens is in a very early alpha release and everything might change.
Don't run it in production, but do play around with it and [contribute](#contributing)
when you can!

# More Details Please!

Although the project is in development, here's where we're going:

The package registry and the edge proxy both implement the [vgo download protocol](https://medium.com/@arschles/project-athens-the-download-protocol-2b346926a818), but each one
is intended for different purposes.

The registry will be hosted globally, and will be "always on" for folks. Anyone will be able to 
configure their machine to do a `go get` (right now, it's a `vgo get`) and have it request
packages from the registry.

On the other hand, the registry will only host _public_ code. If you have private code, the
edge proxy is your jam. The proxy will store your private code for you, in your database
of choice. It will be designed to also cache packages from the registry, subject to
an exclude list.

# Development

See [DEVELOPMENT.md](./DEVELOPMENT.md) for details on how to set up your development environment
and start contributing code.

Speaking of contributing, read on!

# Contributing

This project is early and there's plenty of interesting and challenging work to do.

If you find a bug or want to fix a bug, we :heart: PRs and issues! If you see an issue
in the [queue](https://github.com/gomods/athens/issues) that you'd like to work on, please just post a comment saying that you want to work on it. Something like "I want to work on this" is fine.

If you decide to contribute (we hope you do :smile:), the process is familiar and easy if you've used Github before. There are no long documents to read or complex setup. If you haven't used Github before, the awesome [@bketelsen](https://github.com/bketelsen) has created a good overview on how to contribute code - see [here](https://www.youtube.com/watch?v=bgSDcTyysRc).

Before you do start contributing or otherwise getting involved, we want to let you know that we follow a general [philosophy](./PHILOSOPHY.md) in how we work together, and we'd really appreciate you getting familiar with it before you start.

It's not too long and it's ok for you to "skim" it (or even just read the first two sections :smile:), just as long as you understand the spirit of who we are and how we work.

# Getting Involved

If you're not ready to contribute code yet, there are plenty of other great ways to get involved:

- Come talk to us in the `#athens` channel in the [Gophers slack](http://gophers.slack.com/). We’re a really friendly group, so come say hi and join us! Ping me (`@arschles` on slack) in the channel and I’ll give you the lowdown
- Come to our [weekly development meetings](https://docs.google.com/document/d/1xpvgmR1Fq4iy1j975Tb4H_XjeXUQUOAvn0FximUzvIk/edit#)! They are a great way to meet folks, ask questions, find some stuff to work on, or just hang out if you want to. Just like with this project, absolutely everyone is welcome to join and participate in those
- Get familiar with the system. There's lots to read about. Here are some places to start:
    - [Gentle Introduction to the Project](https://medium.com/@arschles/project-athens-c80606497ce1) - the basics of why we started this project
    - [The Download Protocol](https://medium.com/@arschles/project-athens-the-download-protocol-2b346926a818) - the core API that the registry and proxies implement and CLIs use to download packages
    - [Registry Design](./REGISTRY.md) - what the registry is and how it works
    - [Proxy Design](./PROXY.md) - what the proxy is and how it works
    - [vgo wiki](https://github.com/golang/go/wiki/vgo) - context and details on how Go dependency management works in general
    - ["Go and Versioning"](https://research.swtch.com/vgo) - long papers on Go dependency management details, internals, etc...

# Built on the Shoulders of Giants

The Athens project would not be possible without the amazing projects it builds on. Please see 
[SHOULDERS.md](./SHOULDERS.md) to see a list of them.

# Code of Conduct

This project follows the [Contributor Covenant](https://www.contributor-covenant.org/) (English version [here](https://www.contributor-covenant.org/version/1/4/code-of-conduct)) code of conduct.

If you have concerns, notice a code of conduct violation, or otherwise would like to talk about something
related to this code of conduct, please reach out to me, Aaron Schlesinger on the [Gophers Slack](https://gophers.slack.com/. My username is `arschles`. Note that in the future, we will be expanding the
ways that you can contact us regarding the code of conduct.
