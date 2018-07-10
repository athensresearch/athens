package main

import (
	"fmt"
	"log"

	"github.com/garyburd/redigo/redis"
	"github.com/gobuffalo/buffalo"
	"github.com/gobuffalo/buffalo/worker"
	"github.com/gobuffalo/gocraft-work-adapter"
	"github.com/gomods/athens/cmd/olympus/actions"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/storage"
)

func main() {
	app := setupApp()

	if err := app.Serve(); err != nil {
		log.Fatal(err)
	}
}

func setupApp() *buffalo.App {
	w := getWorker()
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
		Worker:         w,
	}
	app := actions.App(&config)

	if err := w.Register(actions.DownloadHandlerName, actions.GetPackageDownloaderJob(storage, eLog, app.Worker)); err != nil {
		log.Fatal(err)
	}
	if err := w.Register(actions.PushNotificationHandlerName, actions.GetProcessPushNotificationJob(storage, eLog)); err != nil {
		log.Fatal(err)
	}
	return app
}

func getStorage() (storage.Backend, error) {
	storage, err := actions.GetStorage()
	if err != nil {
		return nil, fmt.Errorf("error creating storage (%s)", err)
	}
	if err := storage.Connect(); err != nil {
		return nil, fmt.Errorf("unable to connect to backing store: %v", err)
	}
	return storage, nil
}

func getWorker() worker.Worker {
	port := env.OlympusRedisQueuePortWithDefault(":6379")
	return gwa.New(gwa.Options{
		Pool: &redis.Pool{
			MaxActive: 5,
			MaxIdle:   5,
			Wait:      true,
			Dial: func() (redis.Conn, error) {
				return redis.Dial("tcp", port)
			},
		},
		Name:           actions.OlympusWorkerName,
		MaxConcurrency: 25,
	})
}
