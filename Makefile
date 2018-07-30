.PHONY: build
build:
	cd cmd/proxy && buffalo build
	cd cmd/olympus && buffalo build

.PHONY: run
run: build
	./athens

.PHONY: docs
docs:
	cd docs && hugo

.PHONY: setup-dev-env
setup-dev-env:
	./scripts/get_dev_tools.sh

.PHONY: verify
verify:
	./scripts/check_gofmt.sh
	./scripts/check_golint.sh
	./scripts/check_deps.sh

.PHONY: test
test:
	cd cmd/proxy && buffalo test
	cd cmd/olympus && buffalo test

.PHONY: test-unit
test-unit:
	./scripts/test_unit.sh

.PHONY: olympus-docker
olympus-docker:
	docker build -t gopackages/olympus -f cmd/olympus/Dockerfile .

.PHONY: dev
dev:
	docker-compose -p athensdev up -d mysql
	docker-compose -p athensdev up -d postgres
	docker-compose -p athensdev up -d mongo
	docker-compose -p athensdev up -d redis
	docker-compose -p athensdev up -d minio
	docker-compose -p athensdev up -d jaeger
	echo "sleeping for a bit to wait for the DB to come up"
	sleep 5

.PHONY: dev-teardown
dev-teardown:
	docker-compose -p athensdev down
	docker volume prune
