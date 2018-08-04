#!/bin/bash

# test_e2e.sh
# Execute end-to-end (e2e) tests to verify that everything is working right
# from the end user perpsective
set -xeuo pipefail

REPO_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )/.."

# Use a version of Go that supports Go Modules
export GO111MODULES=on
GOMOD_CACHE=$(go env GOPATH)/pkg/mod
GO_SOURCE=${GO_SOURCE:=$(go env GOPATH)/src/golang.org/x/go}
export GOROOT=${GO_SOURCE}
export PATH=${GO_SOURCE}/bin:${REPO_DIR}/bin:${PATH}
go version

clearGoModCache () {
  # The sudo is a necessary workaround until go is fixed
  sudo rm -fr ${GOMOD_CACHE}
}

teardown () {
  # Cleanup after our tests
  pkill buffalo || true
  popd 2> /dev/null || true
}
trap teardown EXIT

# Start the proxy in the background and wait for it to be ready
export GO_BINARY_PATH=${GO_SOURCE}/bin/go
cd $REPO_DIR/cmd/proxy
pkill buffalo || true # cleanup old buffalos
buffalo dev &
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:3000)" != "200" ]]; do sleep 5; done

# Clone our test repo
TEST_SOURCE=${TMPDIR}go-depmgmt-testrepo
rm -fr ${TEST_SOURCE} 2> /dev/null || true
git clone https://github.com/athens-artifacts/happy-path.git ${TEST_SOURCE}
pushd ${TEST_SOURCE}

clearGoModCache

# Make sure that our test repo works without the GOPROXY first
unset GOPROXY
go run .

clearGoModCache

# Verify that the test works against the proxy
export GOPROXY=http://localhost:3000
go run .
