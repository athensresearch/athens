package fs

import (
	"context"

	"github.com/gomods/athens/pkg/storage"
)

// Delete removes a specific version of a module.
func (v *storageImpl) Delete(ctx context.Context, module, version string) error {
	versionedPath := v.versionLocation(module, version)
	if !v.Exists(ctx, module, version) {
		return storage.ErrVersionNotFound{
			Module:  module,
			Version: version,
		}
	}
	return v.filesystem.RemoveAll(versionedPath)
}
