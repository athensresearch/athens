package actions

import (
	"errors"
	"io/ioutil"
	"path/filepath"
	"time"

	"github.com/gobuffalo/buffalo/worker"
	"github.com/gomods/athens/pkg/eventlog"
	"github.com/gomods/athens/pkg/repo"
	"github.com/gomods/athens/pkg/storage"
	"github.com/spf13/afero"
)

// GetPackageDownloaderJob porcesses queue of cache misses and downloads sources from VCS
func GetPackageDownloaderJob(s storage.Backend, e eventlog.Eventlog, w worker.Worker) worker.Handler {
	return func(args worker.Args) error {
		module, version, err := parseArgs(args)
		if err != nil {
			return err
		}

		// download package
		f, err := repo.NewGenericFetcher(afero.NewOsFs(), module, version)
		if err != nil {
			return err
		}

		dirName, err := f.Fetch()
		if err != nil {
			return err
		}

		modPath := filepath.Join(dirName, version+".mod")
		modBytes, err := ioutil.ReadFile(modPath)
		if err != nil {
			return err
		}

		zipPath := filepath.Join(dirName, version+".zip")
		zipBytes, err := ioutil.ReadFile(zipPath)
		if err != nil {
			return err
		}

		infoPath := filepath.Join(dirName, version+".info")
		infoBytes, err := ioutil.ReadFile(infoPath)
		if err != nil {
			return err
		}

		// save it
		if err := s.Save(module, version, modBytes, zipBytes, infoBytes); err != nil {
			return err
		}

		// update log
		_, err = e.Append(eventlog.Event{Module: module, Version: version, Time: time.Now(), Op: eventlog.OpAdd})
		return err
	}
}

func parseArgs(args worker.Args) (string, string, error) {
	module, ok := args[workerModuleKey].(string)
	if !ok {
		return "", "", errors.New("module name not specified")
	}

	version, ok := args[workerVersionKey].(string)
	if !ok {
		return "", "", errors.New("version not specified")
	}

	return module, version, nil
}
