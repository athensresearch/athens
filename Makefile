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
