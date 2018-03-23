package fs

import (
	"os"
	"path/filepath"
	"time"

	"github.com/spf13/afero"

	"github.com/gomods/athens/pkg/storage"
)

func (v *storageImpl) Get(module, version string) (*storage.Version, error) {
	versionedPath := v.versionLocation(module, version)
	mod, err := afero.ReadFile(v.filesystem, filepath.Join(versionedPath, "go.mod"))
	if err != nil {
		return nil, err
	}

	src, err := v.filesystem.OpenFile(filepath.Join(versionedPath, "source.zip"), os.O_RDONLY, 0666)
	if err != nil {
		return nil, err
	}

	// TODO: store the time in the saver, and parse it here
	return &storage.Version{
		RevInfo: storage.RevInfo{
			Version: version,
			Name:    version,
			Short:   version,
			Time:    time.Now(),
		},
		Mod: mod,
		Zip: src,
	}, nil
}
