package main

import (
	"fmt"
	"log"

	"github.com/gobuffalo/buffalo"
	"github.com/gomods/athens/cmd/olympus/actions"
	"github.com/gomods/athens/pkg/storage"
)

func main() {
	app, err := setupApp()
	if err != nil {
		log.Fatal(err)
	}

	if err := app.Serve(); err != nil {
		log.Fatal(err)
	}
}

func setupApp() (*buffalo.App, error) {
	storage, err := getStorage()
	if err != nil {
		log.Fatalf("error creating storage (%s)", err)
	}
	eLog, err := actions.GetEventLog()
	if err != nil {
		log.Fatalf("error creating eventlog (%s)", err)
	}
	cacheMissesLog, err := actions.NewCacheMissesLog()
	if err != nil {
		log.Fatalf("error creating cachemisses log (%s)", err)
	}

	config := actions.AppConfig{
		Storage:        storage,
		EventLog:       eLog,
		CacheMissesLog: cacheMissesLog,
	}

	return actions.App(&config)
}

func getStorage() (storage.Backend, error) {
	storage, err := actions.GetStorage()
	if err != nil {
		return nil, fmt.Errorf("error creating storage (%s)", err)
	}
	return storage, nil
}
