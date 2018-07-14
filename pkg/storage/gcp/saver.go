package gcp

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"log"

	"cloud.google.com/go/storage"
	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/config/env"
	stg "github.com/gomods/athens/pkg/storage"
	multierror "github.com/hashicorp/go-multierror"
)

// Save uploads the module's .mod, .zip and .info files for a given version
// It expects a context, which can be provided using context.Background
// from the standard library until context has been threaded down the stack.
// see issue: https://github.com/gomods/athens/issues/174
//
// Uploaded files are publicly accessable in the storage bucket as per
// an ACL rule.
func (s *Storage) Save(ctx context.Context, module, version string, mod []byte, zip io.Reader, info []byte) error {
	if exists := s.Exists(module, version); exists {
		return stg.ErrVersionAlreadyExists{Module: module, Version: version}
	}

	errs := make(chan error, 3)
	// create a context that will time out after the value found in
	// the ATHENS_TIMEOUT env variable
	ctxWT, cancelCTX := context.WithTimeout(ctx, env.Timeout())
	defer cancelCTX()

	// dispatch go routine for each file to upload
	go upload(ctxWT, errs, s.bucket, module, version, "mod", "text/plain", bytes.NewReader(mod))
	go upload(ctxWT, errs, s.bucket, module, version, "zip", "application/octet-stream", zip)
	go upload(ctxWT, errs, s.bucket, module, version, "info", "application/json", bytes.NewReader(info))

	var errors error
	// wait for each routine above to send a value
	for count := 0; count < 3; count++ {
		err := <-errs
		if err != nil {
			errors = multierror.Append(errors, err)
		}
	}
	close(errs)

	return errors
}

// upload waits for either writeToBucket to complete or the context expires
func upload(ctx context.Context, errs chan<- error, bkt *storage.BucketHandle, module, version, ext, contentType string, file io.Reader) {
	select {
	case errs <- writeToBucket(ctx, bkt, config.PackageVersionedName(module, version, ext), contentType, file):
		return
	case <-ctx.Done():
		errs <- fmt.Errorf("WARNING: context deadline exceeded during write of %s version %s", module, version)
	}
}

// writeToBucket performs the actual write to a gcp storage bucket
func writeToBucket(ctx context.Context, bkt *storage.BucketHandle, filename, contentType string, file io.Reader) error {
	wc := bkt.Object(filename).NewWriter(ctx)
	defer func(w *storage.Writer) {
		if err := w.Close(); err != nil {
			log.Printf("WARNING: failed to close storage object writer: %s", err)
		}
	}(wc)
	wc.ContentType = contentType
	wc.ACL = []storage.ACLRule{{Entity: storage.AllUsers, Role: storage.RoleReader}}
	if _, err := io.Copy(wc, file); err != nil {
		return err
	}
	return nil
}
