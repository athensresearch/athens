package gcp

import (
	"context"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/storage"
	modupl "github.com/gomods/athens/pkg/storage/module"
)

// Delete implements the (./pkg/storage).Deleter interface and
// removes a version of a module from storage. Returning ErrNotFound
// if the version does not exist.
func (s *Storage) Delete(module, version string) error {
	ctx := context.Background()
	if exists := s.bucket.Exists(ctx, config.PackageVersionedName(module, version, "mod")); !exists {
		return storage.ErrVersionNotFound{Module: module, Version: version}
	}

	return modupl.Delete(ctx, module, version, s.bucket.Delete)
}
