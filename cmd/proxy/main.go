package main

import (
	"log"

	"github.com/gomods/athens/cmd/proxy/actions"
)

func main() {
	app, err := actions.App()
	if err != nil {
		log.Fatal(err)
	}

	if err := app.Serve(); err != nil {
		log.Fatal(err)
	}
}
