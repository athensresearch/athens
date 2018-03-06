cli: 
	go build -o vgp ./cmd/cli

docs:
	cd docs && hugo

test:
	go test ./...
