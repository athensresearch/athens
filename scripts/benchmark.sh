#!/bin/bash

go test -v -bench=. $(find . -iname '*storage*test.go' -not -path '/vendor/') -run=^$
