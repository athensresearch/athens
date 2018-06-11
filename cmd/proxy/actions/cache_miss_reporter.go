package actions

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/url"
	"path"
	"time"

	"github.com/gobuffalo/buffalo/worker"
	"github.com/gomods/athens/pkg/payloads"
)

// GetCacheMissReporterJob porcesses queue of cache misses and reports them to Olympus
func GetCacheMissReporterJob(w worker.Worker) worker.Handler {
	return func(args worker.Args) (err error) {
		module, version, err := parseArgs(args)
		if err != nil {
			return err
		}

		if err := reportCacheMiss(module, version); err != nil {
			return err
		}

		return queueCacheMissFetch(module, version, w)
	}
}

func reportCacheMiss(module, version string) error {
	cm := payloads.Module{Name: module, Version: version}
	content, err := json.Marshal(cm)
	if err != nil {
		return err
	}

	p, err := getCacheMissPath()
	if err != nil {
		return err
	}
	req, err := http.NewRequest("POST", p, bytes.NewBuffer(content))
	if err != nil {
		return err
	}

	client := http.Client{
		Timeout: 30 * time.Second,
	}

	_, err = client.Do(req)
	return err
}

func getCacheMissPath() (string, error) {
	endpoint := GetOlympusEndpoint()
	cacheMissPath := "cachemiss"

	u, err := url.Parse(endpoint)
	if err != nil {
		return "", err
	}
	u.Path = path.Join(u.Path, cacheMissPath)
	return u.String(), nil
}

func queueCacheMissFetch(module, version string, w worker.Worker) error {
	return w.Perform(worker.Job{
		Queue:   workerQueue,
		Handler: FetcherWorkerName,
		Args: worker.Args{
			workerModuleKey:   module,
			workerVersionKey:  version,
			workerTryCountKey: maxTryCount,
		},
	})
}
