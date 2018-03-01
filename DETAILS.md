# Details on Athens functionality.

This document describes the various use cases that Athens supports or will
support. Not everything here is implemented.

[@bketelsen](https://github.com/bketelsen) and I spoke at length on Slack about
how the Athens registry should work. I'm putting our discussion down on "paper"
and adding some of my own detail (and some opinions) so we can grow this into
a definitive document.

# Where Will the Code Come From?

Here's what was discussed on slack + various other forums:

## Travis or Other CI systems (Webhook)

After code passes tests, the publisher can hit a webhook to tell the registry to fetch
code for the given tag from the given repository. The webhook will then do the following:

- Check if the repository is already "known" (the owner of the repository will need to register it prior)
   - Fail if it isn't
- Check if the given tag already exists on the Athens server (we want versions to be immutable)
  - Fail if it does
- Download the code from the given tag and store it in a CDN

We may need to require users to be authorized to hit the webhooks.

This flow is only valid for when Athens is functioning as a registry (see below).

## Manual Uploads

This works similarly to the webhook flow, except a user would upload their module --
including code -- manually to the registry. When they upload, they'll need to
specify the module name and version, and obviously include the zip file with their
source code.

The zip file will need to have the `go.mod` file in it, and the server will
parse it out.

Athens will ship with a CLI to construct and upload the zip file.

This flow is only valid for when Athens is functioning as a registry (see below).

## VCS

Also note that, since Athens can act as a proxy, it may not store any code
and just refer to a VCS repository + tag. See below for details on the
proxy.

This flow is only valid for when Athens is functioning as a proxy (see below).

# How Will Modules Be Served?

We will provide two mechanisms for downloading modules: a proxy and a registry.

## The Proxy

The athens server can be configured to serve `<meta>` tags that effectively
"redirect" the `go get` tool to a specific repository. For example, this
command:

```console
go get gomods.io/github.com/my/thing@v1
```

Will make a request to `https://gomods.io/github.com/my/thing?go-get=1`. Then,
the gomods server will return a page with the meta tag redirect to send
`go get` to the github repository `my/thing` at tag `v1.0.0`.

https://gopkg.in has previously implemented this approach.

## The Registry

We've been able to specify "vanity" names (names that don't match the
repository the code lives in) for our packages for a while now.

https://gopkg.in was one of the earliest systems that let us do this.
It "redirected" (using the `<meta>` tag) the `go get` requests against it to
the appropriate repository in Github. The `go get` tool would then follow the
redirect and use `git` to fetch the code. The athens proxy also uses this
approach.

Now that there's a custom HTTP-based download protocol, we can use the same
rename-by-redirecting mechanism, but with the following additions:

* Allow users to specify their own "vanity" module names for a given codebase:
  we'll allow users to specify at most 1 vanity name attached to an existing
  repository. If they specify a vanity name, we'll allow them to "turn off"
  repository-based (i.e. the `github.com`) name
* Redirect to a CDN instead of to Github

Given that new functionality, the following workflow is possible (assuming the registry
server lives at `gomods.io`):

```console
$ go get gomods.io/my/package@v1
```

The new caninocal import path for this package is `gomods.io/my/package`.
This also allows for import paths to be specified in packages, for example:

```go
package captainhook // import gomods.io/captainhook
```

Since we now allow developers to specify any vanity name they like, we can,
on the registry server, provide a UI to allow developers to manage their
modules, organize them according to naming schemes and groups, manage privacy
for their module names (which is out of the scope of this document), and so on.
