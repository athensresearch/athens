package fs

import (
	"fmt"
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
// If the root directory does not exist an error is returned
func NewStorage(rootDir string, filesystem afero.Fs) (storage.Backend, error) {
	exists, err := afero.Exists(filesystem, rootDir)
	if err != nil {
		return nil, fmt.Errorf("could not check if root directory `%s` exists: %s", rootDir, err)
	}
	if !exists {
		return nil, fmt.Errorf("root directory `%s` does not exist", rootDir)
	}
	return &storageImpl{rootDir: rootDir, filesystem: filesystem}, nil

}
