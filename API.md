# The vgoprox API

This page specifies the API that vgoprox implements.

# The Proxy API

The Proxy API is what the `vgo` client uses to fetch modules. vgoprox implements all of the required APIs, but not the
optional ones:


```console
GET {base_path}/{module}/@t/yyyymmddhhmmss
GET {base_path}/{module}/@t/yyyymmddhhmmss/branch
```

# Other APIs

vgoprox implements a few other APIs that you need to know about to make it useful:

## `POST /admin/upload/{base_path}/{module}/{version}`

This lets you upload a new module, or a new version of an existing module. The request body should look like this:

```json
{"module": "bytes of the go.mod file", "zip": "bytes for the zipped source"}
```

## `GET /all`

This gets all of the registered modules and their versions. This endpoint will be removed or significantly changed
soon.
