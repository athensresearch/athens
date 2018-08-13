#!/bin/bash

set -xeuo pipefail

TAR_GZ="buffalo_0.12.3_linux_amd64.tar.gz"
BUFFALO_URL="https://github.com/gobuffalo/buffalo/releases/download/v0.12.3/${TAR_GZ}"
BUFFALO_TARGET_BIN="./bin/buffalo"

TMPDIR=$(mktemp -d)
curl -L -o ${TMPDIR}${TAR_GZ} ${BUFFALO_URL}
tar -xzf ${TMPDIR}${TAR_GZ} -C ${TMPDIR}
mkdir -p $(dirname ${BUFFALO_TARGET_BIN})
mv ${TMPDIR}/buffalo-no-sqlite ${BUFFALO_TARGET_BIN}
chmod +x ${BUFFALO_TARGET_BIN}
