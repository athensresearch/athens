package fs

import (
	"github.com/gomods/athens/pkg/storage"
)

// Delete removes a specific version of a module.
func (v *storageImpl) Delete(module, version string) error {
	versionedPath := v.versionLocation(module, version)
	if !v.Exists(module, version) {
		return storage.ErrVersionNotFound{
			Module:  module,
			Version: version,
		}
	}
	return v.filesystem.RemoveAll(versionedPath)
}
