#!/bin/bash

# test_e2e.sh
# Execute end-to-end (e2e) tests to verify that everything is working right
# from the end user perpsective
set -xeuo pipefail

REPO_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )/.."

GOMOD_CACHE=$(go env GOPATH)/pkg/mod
export PATH=${REPO_DIR}/bin:${PATH}

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
cd $REPO_DIR/cmd/proxy
pkill buffalo || true # cleanup old buffalos
buffalo dev &
while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:3000)" != "200" ]]; do sleep 5; done

# Clone our test repo
TMPDIR=$(mktemp -d)
TEST_SOURCE=${TMPDIR}happy-path
rm -fr ${TEST_SOURCE} 2> /dev/null || true
git clone https://github.com/athens-artifacts/happy-path.git ${TEST_SOURCE}
pushd ${TEST_SOURCE}

clearGoModCache

# set modules on after running buffalo dev, not sure why
# issue https://github.com/gomods/athens/issues/412
export GO111MODULE=on
# Make sure that our test repo works without the GOPROXY first
unset GOPROXY
go run .

clearGoModCache

# Verify that the test works against the proxy
export GOPROXY=http://localhost:3000
go run .
