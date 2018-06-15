package main

import (
	"fmt"
	"log"

	"github.com/gomods/athens/cmd/olympus/actions"
	"github.com/gomods/athens/pkg/storage"
)

func main() {
	app := actions.App()

	s, err := getLocalStorage()
	if err != nil {
		log.Fatal(err)
	}

	e, err := actions.GetEventLog()
	if err != nil {
		log.Fatal(err)
	}

	w := app.Worker
	if err := w.Register(actions.DownloadWorkerName, actions.GetPackageDownloaderJob(s, e, w)); err != nil {
		log.Fatal(err)
	}

	if err := app.Serve(); err != nil {
		log.Fatal(err)
	}
}

func getLocalStorage() (storage.Backend, error) {
	s, err := actions.GetStorage()
	if err != nil {
		return nil, fmt.Errorf("Unable to retrieve backing store: %v", err)
	}

	if err := s.Connect(); err != nil {
		return nil, fmt.Errorf("Unable to connect to backing store: %v", err)
	}

	return s, nil
}
