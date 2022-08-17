#!/usr/bin/env bash

# Fail on all sorts of errors.
# https://stackoverflow.com/a/2871034/2116927
set -euxo pipefail

# In Vercel -> Project Settings -> Build & Development Settings:
# Build command   : yarn vercel:build
# Output directory: vercel-static
# Install command : yarn vercel:install
#
# In Vercel -> Project Settings -> Git
# Release branch: dummy-vercel-web
# Pre-release branch: dummy-vercel-beta
# These are dummy branches that we do not push builds to.
# Instead the `release-web` github actions job manually builds and deploys a prod build when needed.
# The build settings above are still used for the prod build though, and the
# vercel-release/package.json file is meant to provide noop scripts for it.

# See https://vercel.com/docs/concepts/deployments/build-step#build-image for custom setup instructions.

# Java 11 is already installed.
java --version

# Clojure linux installer.
curl -O https://download.clojure.org/install/linux-install-1.10.3.1040.sh
chmod +x linux-install-1.10.3.1040.sh
./linux-install-1.10.3.1040.sh
clojure --version
