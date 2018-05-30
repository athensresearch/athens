# The Athens CLI

The Athens registry provides a crude "admin" API that allows you to upload new
versions of modules to the [registry](./REGISTRY.md).

_Note: The API is currently crude mostly because it currently is not protected
by authentication or authorization. Everybody has "god mode"!_

Athens provides a basic CLI for the registry that makes it convenient to
upload a new module, or new version of a module, to the server. Find the code
for it in the [./cli](./cmd/cli) directory, and build it with the following:

```console
make cli
```

When the build completes, you'll get a `athens` binary in the same directory.
The binary is limited right now. Run it like this:

```console
./athens <directory> <baseURL> <module> <version>
```

A few additional notes on this CLI:

* It is hard coded to make requests against `http://localhost:3000`, so you'll need to have the
  Athens server running to successfully use it. See
  [Development](./README.md#development) in the README for details on how to
  do that
* `<directory>` will be zipped up and uploaded to the Athens server
* ... and it needs to have a `go.mod` file in its root
* The go.mod file's 'module' directive must match `<module>`. `athens` won't
  currently read the go.mod file for you (that's planned though)
* If there are any `vendor` directories under `<directory>`, they won't be
  ignored yet (that's planned though)
