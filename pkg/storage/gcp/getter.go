package gcp

import (
	"context"
	"fmt"
	"io"
	"io/ioutil"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/errors"
	opentracing "github.com/opentracing/opentracing-go"
)

// Info implements storage.Getter
func (s *Storage) Info(ctx context.Context, module, version string) ([]byte, error) {
	const op errors.Op = "gcp.Info"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.gcp.Info")
	defer sp.Finish()
	exists, err := s.Exists(ctx, module, version)
	if err != nil {
		return nil, errors.E(op, err, errors.M(module), errors.V(version))
	}
	if !exists {
		return nil, errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
	}

	infoReader, err := s.bucket.Open(ctx, config.PackageVersionedName(module, version, "info"))
	if err != nil {
		return nil, errors.E(op, err, errors.M(module), errors.V(version))
	}
	infoBytes, err := ioutil.ReadAll(infoReader)
	infoReader.Close()
	if err != nil {
		return nil, errors.E(op, err, errors.M(module), errors.V(version))
	}
	return infoBytes, nil
}

// GoMod implements storage.Getter
func (s *Storage) GoMod(ctx context.Context, module, version string) ([]byte, error) {
	const op errors.Op = "gcp.GoMod"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.gcp.GoMod")
	defer sp.Finish()
	exists, err := s.Exists(ctx, module, version)
	if err != nil {
		return nil, errors.E(op, err, errors.M(module), errors.V(version))
	}
	if !exists {
		return nil, errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
	}

	modReader, err := s.bucket.Open(ctx, config.PackageVersionedName(module, version, "mod"))
	if err != nil {
		return nil, errors.E(op, err, errors.M(module), errors.V(version))
	}
	modBytes, err := ioutil.ReadAll(modReader)
	modReader.Close()
	if err != nil {
		return nil, errors.E(op, fmt.Errorf("could not get new reader for mod file: %s", err), errors.M(module), errors.V(version))
	}

	return modBytes, nil
}

// Zip implements storage.Getter
func (s *Storage) Zip(ctx context.Context, module, version string) (io.ReadCloser, error) {
	const op errors.Op = "gcp.Zip"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.gcp.Zip")
	defer sp.Finish()
	exists, err := s.Exists(ctx, module, version)
	if err != nil {
		return nil, errors.E(op, err, errors.M(module), errors.V(version))
	}
	if !exists {
		return nil, errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
	}

	zipReader, err := s.bucket.Open(ctx, config.PackageVersionedName(module, version, "zip"))
	if err != nil {
		return nil, errors.E(op, err, errors.M(module), errors.V(version))
	}

	return zipReader, nil
}
