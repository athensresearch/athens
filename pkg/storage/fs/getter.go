package fs

import (
	"os"
	"path/filepath"

	"github.com/gomods/athens/pkg/storage"
	"github.com/spf13/afero"
)

func (v *storageImpl) Get(module, version string) (*storage.Version, error) {
	versionedPath := v.versionLocation(module, version)
	mod, err := afero.ReadFile(v.filesystem, filepath.Join(versionedPath, "go.mod"))
	if err != nil {
		return nil, storage.ErrVersionNotFound{Module: module, Version: version}
	}

	src, err := v.filesystem.OpenFile(filepath.Join(versionedPath, "source.zip"), os.O_RDONLY, 0666)
	if err != nil {
		return nil, storage.ErrVersionNotFound{Module: module, Version: version}
	}

	info, err := afero.ReadFile(v.filesystem, filepath.Join(versionedPath, version+".info"))
	if err != nil {
		return nil, storage.ErrVersionNotFound{Module: module, Version: version}
	}

	// TODO: store the time in the saver, and parse it here
	return &storage.Version{
		Mod:  mod,
		Zip:  src,
		Info: info,
	}, nil
}
