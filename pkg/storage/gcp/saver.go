package gcp

import (
	"bytes"
	"context"
	"io"
	"log"

	stg "github.com/gomods/athens/pkg/storage"
	moduploader "github.com/gomods/athens/pkg/storage/module"
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

	err := moduploader.Upload(ctx, module, version, bytes.NewReader(info), bytes.NewReader(mod), zip, s.upload)
	// TODO: take out lease on the /list file and add the version to it
	//
	// Do that only after module source+metadata is uploaded
	return err
}

func (s *Storage) upload(ctx context.Context, path, contentType string, stream io.Reader) error {
	wc := s.bucket.Write(ctx, path)
	defer func(wc io.WriteCloser) {
		if err := wc.Close(); err != nil {
			log.Printf("WARNING: failed to close storage object writer: %s", err)
		}
	}(wc)
	// NOTE: content type is auto detected on GCP side and ACL defaults to public
	// Once we support private storage buckets this may need refactoring
	// unless there is a way to set the default perms in the project.
	if _, err := io.Copy(wc, stream); err != nil {
		return err
	}
	return nil
}
