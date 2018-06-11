package main

import (
	"fmt"
	"log"

	"github.com/gomods/athens/cmd/proxy/actions"
	"github.com/gomods/athens/pkg/storage"
)

func main() {
	app, err := actions.App()
	if err != nil {
		log.Fatal(err)
	}

	s, err := getLocalStorage()
	if err != nil {
		log.Fatal(err)
	}

	w := app.Worker
	if err := w.Register(actions.FetcherWorkerName, actions.GetProcessCacheMissJob(s, w)); err != nil {
		log.Fatal(err)
	}
	if err := w.Register(actions.ReporterWorkerName, actions.GetCacheMissReporterJob(w)); err != nil {
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
