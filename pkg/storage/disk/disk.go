package disk

import (
	"path/filepath"

	"github.com/gomods/athens/pkg/storage"
)

// Storage is the only interface defined by the disk storage. Use
// NewStorage to create one of these. Everything is all in one
// because it all has to share the same tree
type Storage interface {
	storage.Lister
	storage.Getter
	storage.Saver
}

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
func NewStorage(rootDir string) Storage {
	return &storageImpl{rootDir: rootDir}

}
