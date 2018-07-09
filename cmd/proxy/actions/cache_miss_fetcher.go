package actions

import (
	"context"
	"errors"
	"fmt"

	"github.com/gobuffalo/buffalo/worker"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/module"
	"github.com/gomods/athens/pkg/storage"
	olympusStore "github.com/gomods/athens/pkg/storage/olympus"
)

const (
	// OlympusGlobalEndpoint is a default olympus DNS address
	OlympusGlobalEndpoint = "olympus.gomods.io"
	// OlympusGlobalEndpointOverrideKey overrides default olympus settings
	OlympusGlobalEndpointOverrideKey = "OLYMPUS_GLOBAL_ENDPOINT"
)

// GetProcessCacheMissJob processes queue of cache misses and downloads sources from active Olympus
func GetProcessCacheMissJob(s storage.Backend, w worker.Worker, mf *module.Filter) worker.Handler {
	return func(args worker.Args) (err error) {
		mod, version, err := parseArgs(args)
		if err != nil {
			return err
		}

		if !mf.ShouldProcess(mod) {
			return module.NewErrModuleExcluded(mod)
		}

		if s.Exists(mod, version) {
			return nil
		}

		// get module info
		v, err := getModuleInfo(mod, version)
		if err != nil {
			process(mod, version, args, w)
			return err
		}
		defer v.Zip.Close()

		if err := s.Save(context.Background(), mod, version, v.Mod, v.Zip, v.Info); err != nil {
			process(mod, version, args, w)
		}

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

func getModuleInfo(module, version string) (*storage.Version, error) {
	os := olympusStore.NewStorage(GetOlympusEndpoint())
	return os.Get(module, version)
}

// process pushes pull job into the queue to be processed asynchonously
func process(module, version string, args worker.Args, w worker.Worker) error {
	// decrementing avoids endless loop of entries with missing trycount
	trycount, ok := args[workerTryCountKey].(int)
	if !ok {
		return fmt.Errorf("Trycount missing or invalid")
	}

	if trycount <= 0 {
		return fmt.Errorf("Max trycount for %s %s reached", module, version)
	}

	return w.Perform(worker.Job{
		Queue:   workerQueue,
		Handler: FetcherWorkerName,
		Args: worker.Args{
			workerModuleKey:   module,
			workerVersionKey:  version,
			workerTryCountKey: trycount - 1,
		},
	})
}

// GetOlympusEndpoint returns global endpoint with override in mind
func GetOlympusEndpoint() string {
	return env.OlympusGlobalEndpointWithDefault(OlympusGlobalEndpoint)
}
