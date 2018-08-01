package gcp

import (
	"context"
	"fmt"
	"io/ioutil"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/errors"
	"github.com/gomods/athens/pkg/storage"
	opentracing "github.com/opentracing/opentracing-go"
)

// Get retrieves a module at a specific version from storage as a (./pkg/storage).Version
//
// The caller is responsible for calling close on the Zip ReadCloser
func (s *Storage) Get(ctx context.Context, module, version string) (*storage.Version, error) {
	const op errors.Op = "gcp.Get"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.gcp.Get")
	defer sp.Finish()
	if !s.Exists(ctx, module, version) {
		return nil, errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
	}

	modReader, err := s.bucket.Open(ctx, config.PackageVersionedName(module, version, "mod"))
	if err != nil {
		return nil, errors.E(op, err)
	}
	modBytes, err := ioutil.ReadAll(modReader)
	modReader.Close()
	if err != nil {
		return nil, errors.E(op, fmt.Errorf("could not get new reader for mod file: %s", err))
	}

	zipReader, err := s.bucket.Open(ctx, config.PackageVersionedName(module, version, "zip"))
	// It is up to the caller to call Close on this reader.
	// The storage.Version contains a ReadCloser for the zip.
	if err != nil {
		return nil, errors.E(op, err)
	}

	infoReader, err := s.bucket.Open(ctx, config.PackageVersionedName(module, version, "info"))
	if err != nil {
		return nil, errors.E(op, err)
	}
	infoBytes, err := ioutil.ReadAll(infoReader)
	infoReader.Close()
	if err != nil {
		return nil, errors.E(op, fmt.Errorf("could not read bytes of info file: %s", err))
	}
	return &storage.Version{Mod: modBytes, Zip: zipReader, Info: infoBytes}, nil
}
