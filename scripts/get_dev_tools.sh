#!/bin/bash

# install_dev_deps.sh
# Ensure that the tools needed to build locally are present
set -xeuo pipefail

go get github.com/golang/lint/golint
go get github.com/golang/dep/cmd/dep
go get -u -v golang.org/x/vgo
./scripts/get_buffalo.sh
