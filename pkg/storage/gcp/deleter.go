package gcp

import (
	"context"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/errors"
	modupl "github.com/gomods/athens/pkg/storage/module"
	opentracing "github.com/opentracing/opentracing-go"
)

// Delete implements the (./pkg/storage).Deleter interface and
// removes a version of a module from storage. Returning ErrNotFound
// if the version does not exist.
func (s *Storage) Delete(ctx context.Context, module, version string) error {
	const op errors.Op = "gcp.Delete"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.gcp.Delete")
	defer sp.Finish()
	if exists := s.bucket.Exists(ctx, config.PackageVersionedName(module, version, "mod")); !exists {
		return errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
	}

	return modupl.Delete(ctx, module, version, s.bucket.Delete)
}
