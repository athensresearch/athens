package gcp

import (
	"context"
	"fmt"
	"io/ioutil"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/storage"
	opentracing "github.com/opentracing/opentracing-go"
)

// Get retrieves a module at a specific version from storage as a (./pkg/storage).Version
//
// The caller is responsible for calling close on the Zip ReadCloser
func (s *Storage) Get(ctx context.Context, module, version string) (*storage.Version, error) {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.gcp.Get")
	defer sp.Finish()
	if exists := s.Exists(ctx, module, version); !exists {
		return nil, storage.ErrVersionNotFound{Module: module, Version: version}
	}

	modReader, err := s.bucket.Open(ctx, config.PackageVersionedName(module, version, "mod"))
	if err != nil {
		return nil, fmt.Errorf("could not get new reader for mod file: %s", err)
	}
	modBytes, err := ioutil.ReadAll(modReader)
	modReader.Close()
	if err != nil {
		return nil, fmt.Errorf("could not read bytes of mod file: %s", err)
	}

	zipReader, err := s.bucket.Open(ctx, config.PackageVersionedName(module, version, "zip"))
	// It is up to the caller to call Close on this reader.
	// The storage.Version contains a ReadCloser for the zip.
	if err != nil {
		return nil, fmt.Errorf("could not get new reader for zip file: %s", err)
	}

	infoReader, err := s.bucket.Open(ctx, config.PackageVersionedName(module, version, "info"))
	if err != nil {
		return nil, fmt.Errorf("could not get new reader for info file: %s", err)
	}
	infoBytes, err := ioutil.ReadAll(infoReader)
	infoReader.Close()
	if err != nil {
		return nil, fmt.Errorf("could not read bytes of info file: %s", err)
	}
	return &storage.Version{Mod: modBytes, Zip: zipReader, Info: infoBytes}, nil
}
