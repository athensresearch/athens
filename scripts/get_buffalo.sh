#!/bin/bash

TAR_GZ="buffalo_0.12.3_linux_amd64.tar.gz"
BUFFALO_URL="https://github.com/gobuffalo/buffalo/releases/download/v0.12.3/${TAR_GZ}"
BUFFALO_TARGET_BIN="./bin/buffalo"

curl -L -o ${TAR_GZ} ${BUFFALO_URL}
tar -xvf ${TAR_GZ}
mv buffalo-no-sqlite ${BUFFALO_TARGET_BIN}
chmod +x ${BUFFALO_TARGET_BIN}
