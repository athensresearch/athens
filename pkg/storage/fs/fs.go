package fs

import (
	"path/filepath"

	"github.com/gomods/athens/pkg/storage"
	"github.com/spf13/afero"
)

type storageImpl struct {
	rootDir    string
	filesystem afero.Fs
}

func (s *storageImpl) moduleLocation(module string) string {
	return filepath.Join(s.rootDir, module)
}

func (s *storageImpl) versionLocation(module, version string) string {
	return filepath.Join(s.moduleLocation(module), version)

}

// NewStorage returns a new ListerSaver implementation that stores
// everything under rootDir
func NewStorage(rootDir string, filesystem afero.Fs) storage.Storage {
	return &storageImpl{rootDir: rootDir, filesystem: filesystem}

}
