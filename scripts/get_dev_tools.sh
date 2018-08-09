#!/bin/bash

# install_dev_deps.sh
# Ensure that the tools needed to build locally are present
set -xeuo pipefail

go get github.com/golang/lint/golint
go get github.com/golang/dep/cmd/dep

./scripts/get_buffalo.sh
