# Welcome Gophers!

Welcome to the Athens project! We're building all things Go package repository in here. 

1. [Package Registry](https://github.com/gomods/athens/wiki/The-Central-Package-Registry-(Olympus))
2. [Edge Proxy](https://github.com/gomods/athens/wiki/Proxies-(Zeus))

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

If you find a bug or want to fix a bug, I :heart: PRs and issues! If you see an issue
in the [queue](https://github.com/gomods/athens/issues) that you'd like to work on, please just post a comment saying that you want to work on it. Something like "I want to work on this" is fine.

# Resources:

* ["Go and Versioning"](https://research.swtch.com/vgo) papers
* [vgo wiki](https://github.com/golang/go/wiki/vgo)

# Code of Conduct

This project follows the [Contributor Covenant](https://www.contributor-covenant.org/) (English version [here](https://www.contributor-covenant.org/version/1/4/code-of-conduct)) code of conduct.

If you have concerns, notice a code of conduct violation, or otherwise would like to talk about something
related to this code of conduct, please reach out to me, Aaron Schlesinger on the [Gophers Slack](https://gophers.slack.com/. My username is `arschles`. Note that in the future, we will be expanding the
ways that you can contact us regarding the code of conduct.
