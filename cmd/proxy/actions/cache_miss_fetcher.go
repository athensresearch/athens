package actions

import (
	"context"
	"errors"

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
func GetProcessCacheMissJob(ctx context.Context, s storage.Backend, w worker.Worker, mf *module.Filter) worker.Handler {
	return func(args worker.Args) (err error) {
		mod, version, err := parseArgs(args)
		if err != nil {
			return err
		}

		if s.Exists(ctx, mod, version) {
			return nil
		}

		// get module info
		v, err := getModuleInfo(ctx, mod, version)
		if err != nil {
			return err
		}
		defer v.Zip.Close()

		return s.Save(ctx, mod, version, v.Mod, v.Zip, v.Info)
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

func getModuleInfo(ctx context.Context, module, version string) (*storage.Version, error) {
	os := olympusStore.NewStorage(GetOlympusEndpoint())
	var v storage.Version
	var err error
	v.Info, err = os.Info(ctx, module, version)
	if err != nil {
		return nil, err
	}
	v.Mod, err = os.GoMod(ctx, module, version)
	if err != nil {
		return nil, err
	}
	v.Zip, err = os.Zip(ctx, module, version)
	if err != nil {
		return nil, err
	}
	return &v, nil
}

// GetOlympusEndpoint returns global endpoint with override in mind
func GetOlympusEndpoint() string {
	return env.OlympusGlobalEndpointWithDefault(OlympusGlobalEndpoint)
}
