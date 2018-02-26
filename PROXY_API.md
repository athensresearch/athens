# The vgo Proxy API

This page specifies the API that vgoprox implements. It's mostly a copy/paste from 
[here](https://research.swtch.com/vgo-module) (see "Download Protocol"), but there are a few extra notes in here
that are specific to vgoprox.

# `GET /{base_path}/{module}/@v/list`

This just fetches all the versions for `base_path/module`, with one version on each line. For example:

```
v0.0.1
v0.1.0
v1.0.0
v1.0.1
```

# `GET /{base_path}/{module}/@v/{version}.info`

Fetch the version information for `base_path/module` at version `version`. The response looks like this 
(taken from (Russ's example module](https://storage.googleapis.com/gomodules/rsc/swtch.com/testmod/@v/v1.0.0.info)):

```json
{"Name": "v1.0.0", "Short": "v1.0.0", "Version": "v1.0.0", "Time": "1972-07-18T12:34:56Z"}
```

The corresponding Go struct is this:

```go
type RevInfo struct {
	Version string    // version string
	Name    string    // complete ID in underlying repository
	Short   string    // shortened ID, for use in pseudo-version
	Time    time.Time // commit time
}
```

# `GET /{base_path}/{module}/@v/{version}.mod`

Just returns the `go.mod` file for `base_path/module` at version `version`. For example (taken from 
[Russ's example module](https://storage.googleapis.com/gomodules/rsc/swtch.com/testmod/@v/v1.0.0.mod)):

```
module "swtch.com/testmod"
```

# `GET /{base_path}/{module}/@v/{version}.zip`

Gets the source tree for the `base_path/module` at version `version`
