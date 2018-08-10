Hurray! We are glad that you want to contribute to our project! 👍

If this is your first contribution, not to worry! We have a great [tutorial](https://www.youtube.com/watch?v=bgSDcTyysRc) to help you get started, and you can always ask us for help in the `#athens` channel in the [gopher slack](https://invite.slack.golangbridge.org/). We'll give you whatever guidance you need.

## Verify your work
Run `make verify` to run all the same validations that our CI process runs, such
as checking that the standard go formatting is applied, linting, etc.

## Setup your dev environment

Run `make setup-dev-env` to install local developer tools and run necessary
services, such as mongodb, for the end-to-end tests.

## Unit Tests
Run `make test-unit` to run the unit tests.

## End-to-End Tests
End-to-End tests (e2e) are tests from the user perspective that validate that
everything works when running real live servers, and using `go` with GOPROXY set.

Run `make test-e2e` to run the end-to-end tests.

The first time you run the tests,
you must run `make setup-dev-env` first, otherwise you will see errors like the one below:

```
error connecting to storage (no reachable servers)
```
