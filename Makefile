build:
	cd cmd/proxy && buffalo build

run: build
	./athens
	
cli:
	go build -o athens ./cmd/cli

docs:
	cd docs && hugo

test:
	cd cmd/proxy && buffalo test
	cd cmd/olympus && buffalo test

olympus-docker:
	docker build -t gopackages/olympus -f cmd/olympus/Dockerfile .

dev:
	# TODO: spin up all the services :)
	# need to create an umbrella service in
	# the docker compose file that declares all the
	# deps
	docker-compose -p athensdev up -d mysql
	echo "sleeping for a bit to wait for the DB to come up"
	sleep 5
