package disk

import (
	"path/filepath"

	"github.com/gomods/athens/pkg/storage"
)

type storageImpl struct {
	rootDir string
}

func (s *storageImpl) moduleDiskLocation(baseURL, module string) string {
	return filepath.Join(s.rootDir, baseURL, module)
}

func (s *storageImpl) versionDiskLocation(baseURL, module, version string) string {
	return filepath.Join(s.moduleDiskLocation(baseURL, module), version)

}

// NewStorage returns a new ListerSaver implementation that stores
// everything under rootDir
func NewStorage(rootDir string) storage.Storage {
	return &storageImpl{rootDir: rootDir}
}
