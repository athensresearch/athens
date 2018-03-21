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

func (s *storageImpl) moduleLocation(baseURL, module string) string {
	return filepath.Join(s.rootDir, baseURL, module)
}

func (s *storageImpl) versionLocation(baseURL, module, version string) string {
	return filepath.Join(s.moduleLocation(baseURL, module), version)

}

// NewStorage returns a new ListerSaver implementation that stores
// everything under rootDir
func NewStorage(rootDir string, filesystem afero.Fs) storage.Storage {
	return &storageImpl{rootDir: rootDir, filesystem: filesystem}

}
