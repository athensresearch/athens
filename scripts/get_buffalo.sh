#!/bin/bash

set -xeo pipefail

case "$TRAVIS" in
true)
	VERSION=0.12.4
	TAR_GZ="buffalo_${VERSION}_linux_amd64.tar.gz"
	URL="https://github.com/gobuffalo/buffalo/releases/download/v${VERSION}/${TAR_GZ}"
	TARGET_BIN="$(pwd)/bin/buffalo"
	TMPDIR=$(mktemp -d)

	(
		cd $TMPDIR
		curl -L -o ${TAR_GZ} ${URL}
		tar -xzf ${TAR_GZ}
		mkdir -p $(dirname ${TARGET_BIN})
		cp buffalo-no-sqlite ${TARGET_BIN}
		chmod +x ${TARGET_BIN}
	)
	rm -r $TMPDIR
	;;
*)
	go get github.com/gobuffalo/buffalo/buffalo
	;;
esac
