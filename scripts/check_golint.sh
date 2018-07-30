#!/bin/bash

# check_golint.sh
# Run the linter on everything except generated code
set -euo pipefail

golint -set_exit_status $(go list ./... | grep -v '/mocks')
