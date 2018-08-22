package actions

import (
	"context"

	"github.com/gobuffalo/buffalo/worker"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/storage"
	olympusStore "github.com/gomods/athens/pkg/storage/olympus"
)

const (
	op errors.Op = "GetProcessCacheMissJob"
)

// GetProcessCacheMissJob processes queue of cache misses and downloads sources from active Olympus
func GetProcessCacheMissJob(ctx context.Context, s storage.Backend, w worker.Worker) worker.Handler {
	return func(args worker.Args) (err error) {
		mod, version, err := parseArgs(args)
		if err != nil {
			return err
		}

		moduleExists, err := s.Exists(ctx, mod, version)
		if err != nil {
			return errors.E(op, err)
		}
		if moduleExists {
			return nil
		}

		// get module info
		v, err := getModuleInfo(ctx, mod, version)
		if err != nil {
			return errors.E(op, err)
		}
		defer v.Zip.Close()

		return s.Save(ctx, mod, version, v.Mod, v.Zip, v.Info)
	}
}

func parseArgs(args worker.Args) (string, string, error) {
	module, ok := args[workerModuleKey].(string)
	if !ok {
		return "", "", errors.E(op, "module name not specified")
	}

	version, ok := args[workerVersionKey].(string)
	if !ok {
		return "", "", errors.E(op, "module name not specified")
	}

	return module, version, nil
}

func getModuleInfo(ctx context.Context, module, version string) (*storage.Version, error) {
	os := olympusStore.NewStorage(env.GetOlympusEndpoint())
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
