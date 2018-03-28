package main

import (
	"log"

	"github.com/gomods/athens/cmd/proxy/actions"
)

func main() {
	app := actions.App()
	if err := app.Serve(); err != nil {
		log.Fatal(err)
	}
}
