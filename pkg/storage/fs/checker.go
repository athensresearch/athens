package fs

import (
	"context"
	"path/filepath"

	"github.com/spf13/afero"
)

func (v *storageImpl) Exists(ctx context.Context, module, version string) bool {
	versionedPath := v.versionLocation(module, version)
	exists, err := afero.Exists(v.filesystem, filepath.Join(versionedPath, "go.mod"))
	if err != nil {
		return false
	}

	return exists
}
