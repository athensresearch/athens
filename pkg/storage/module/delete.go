package module

import (
	"context"
	"fmt"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/config/env"
	multierror "github.com/hashicorp/go-multierror"
)

// Deleter takes a path to a file and deletes it from the blob store
type Deleter func(ctx context.Context, path string) error

// Delete deletes .info, .mod and .zip files from the blob store in parallel.
// Returns multierror containing errors from all deletes and timeouts
func Delete(ctx context.Context, module, version string, delete Deleter) error {
	tctx, cancel := context.WithTimeout(ctx, env.Timeout())
	defer cancel()

	del := func(ext string) <-chan error {
		ec := make(chan error)

		go func() {
			defer close(ec)
			p := config.PackageVersionedName(module, version, ext)
			ec <- delete(tctx, p)
		}()
		return ec
	}

	errChan := make(chan error, numFiles)
	delOrAbort := func(ext string) {
		select {
		case err := <-del(ext):
			errChan <- err
		case <-tctx.Done():
			errChan <- fmt.Errorf("deleting %s.%s.%s failed: %s", module, version, ext, tctx.Err())
		}
	}

	go delOrAbort("info")
	go delOrAbort("mod")
	go delOrAbort("zip")

	var errors error
	for i := 0; i < numFiles; i++ {
		err := <-errChan
		if err != nil {
			errors = multierror.Append(errors, err)
		}
	}
	close(errChan)

	return errors
}
