package module

import (
	"context"
	"fmt"
	"io"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/errors"
	multierror "github.com/hashicorp/go-multierror"
)

const numFiles = 3

// Uploader takes a stream and saves it to the blob store under a given path
type Uploader func(ctx context.Context, path, contentType string, stream io.Reader) error

// Upload saves .info, .mod and .zip files to the blob store in parallel.
// Returns multierror containing errors from all uploads and timeouts
func Upload(ctx context.Context, module, version string, info, mod, zip io.Reader, uploader Uploader) error {
	const op errors.Op = "module.Upload"
	tctx, cancel := context.WithTimeout(ctx, env.Timeout())
	defer cancel()

	save := func(ext, contentType string, stream io.Reader) <-chan error {
		ec := make(chan error)

		go func() {
			defer close(ec)
			p := config.PackageVersionedName(module, version, ext)
			ec <- uploader(tctx, p, contentType, stream)
		}()
		return ec
	}

	errChan := make(chan error, numFiles)
	saveOrAbort := func(ext, contentType string, stream io.Reader) {
		select {
		case err := <-save(ext, contentType, stream):
			errChan <- err
		case <-tctx.Done():
			errChan <- fmt.Errorf("uploading %s.%s.%s failed: %s", module, version, ext, tctx.Err())
		}
	}
	go saveOrAbort("info", "application/json", info)
	go saveOrAbort("mod", "text/plain", mod)
	go saveOrAbort("zip", "application/octet-stream", zip)

	var errs error
	for i := 0; i < numFiles; i++ {
		err := <-errChan
		if err != nil {
			errs = multierror.Append(errs, err)
		}
	}
	close(errChan)
	if errs != nil {
		return errors.E(op, errs)
	}

	return nil
}
