#!/usr/bin/env bash

# Fail on all sorts of errors.
# https://stackoverflow.com/a/2871034/2116927
set -euxo pipefail

# In Vercel -> Project Settings -> Build & Development Settings:
# Build command   : yarn vercel:build
# Output directory: vercel-static
# Install command : yarn vercel:install

# See https://vercel.com/docs/concepts/deployments/build-step#build-image for custom setup instructions.

# Java 11 is already installed.
java --version

# Clojure linux installer.
curl -O https://download.clojure.org/install/linux-install-1.10.3.1040.sh
chmod +x linux-install-1.10.3.1040.sh
./linux-install-1.10.3.1040.sh
clojure --version
