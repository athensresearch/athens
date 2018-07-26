package module

import (
	"context"

	"github.com/gomods/athens/pkg/storage"
)

// Fetch downloads the module@version using the fetcher and stores it in storage
func Fetch(ctx context.Context, s storage.Backend, fetcher Fetcher, mod, version string, mf *Filter) error {
	if !mf.ShouldProcess(mod) {
		return NewErrModuleExcluded(mod)
	}

	if s.Exists(ctx, mod, version) {
		return NewErrModuleAlreadyFetched("module.Fetch", mod, version)
	}

	moduleRef, err := fetcher.Fetch(mod, version)
	if err != nil {
		return err
	}
	defer moduleRef.Clear()

	// pretend like moduleLoc has $version.info, $version.mod and $version.zip in it :)
	module, err := moduleRef.Read()
	if err != nil {
		return err
	}
	defer module.Zip.Close()

	return s.Save(context.Background(), mod, version, module.Mod, module.Zip, module.Info)
}
