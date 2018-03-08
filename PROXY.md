# The Athens Proxy

Athens comes with a module proxy that you can run inside your own organization.
This proxy will do two things for you:

* Cache modules locally, so that developers inside your organization do not
  have to fetch modules from the public internet
* Redirect module fetch operations to a specific location on the internet

It's intended that clients set the `GOPROXY` environment variable on their
machine to the Athens proxy address before they start developing. That
enables the following:

```console
go get github.com/a/b
```

This command will go fetch the `github.com/a/b` module from the proxy,
instead of the VCS.

# Proxy Configuration

TODO

# Proxy Caches

The proxy can be configured to store cached modules in several places:

* Memory: this is only for development purposes
* Disk: this is appropriate for single-node deployments. We don't recommend
  running an athens proxy on a single node, so we don't recomment using disk
  storage in a large scale deployment. _This backend is not yet complete_
* RDBMS: this is appropriate for multi-node deployments. We recommend this
  storage backend for larger scale deployments. _This backend is not yet complete_

The proxy storage drivers are responsible for storing _all_ information
concerning a module:

* The module metadata (versions, etc...)
* The module's `go.mod` file
* The module source code (in a Zip file)

Additionally, Athens can be configured to automatically fill its caches on a
cache miss. For example, if the following request is made with `GOPROXY` set
to the proxy's address:

```console
go get github.com/a/b@v1
```

... and `github.com/a/b` is not in the cache, this is a cache miss. The proxy
will return a `404 Not Found` HTTP status code, and fetch `github.com/a/b` from
the VCS in the background. The `404` will make `go get` fetch source from the
VCS, and after the proxy fills the cache, future `go get` operations will
succeed.

# Proxy Redirects

The athens proxy can be configured to redirect various module downloads to
upstream VCS repositories, proxies, or registries. _Redirection is not yet
implemented._
