#!/bin/bash
set -e
clear

verbose=""

echo $@

if [[ "$@" == "-v" ]]
then
  verbose="-v"
fi

docker-compose up -d
sleep 4 # Ensure mysql is online

go get -v -tags sqlite github.com/gobuffalo/pop/...
# go build -v -tags sqlite -o tsoda ./soda

function test {
  echo "!!! Testing $1"
  export SODA_DIALECT=$1
  soda drop -e $SODA_DIALECT -c ./database.yml
  soda create -e $SODA_DIALECT -c ./database.yml
  soda migrate -e $SODA_DIALECT -c ./database.yml
  go test -tags sqlite $verbose $(go list ./... | grep -v /vendor/)
}

test "postgres"
test "cockroach"
test "mysql"
test "sqlite"

docker-compose down

find ./sql_scripts/sqlite -name *.sqlite* -delete
