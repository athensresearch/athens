package mem

import (
	"fmt"
	"sync"

	"github.com/gomods/athens/pkg/cdn/metadata"
	"github.com/gomods/athens/pkg/cdn/metadata/fs"
	"github.com/gomods/athens/pkg/errors"
	"github.com/spf13/afero"
)

var (
	l          sync.Mutex
	memStorage metadata.Storage
)

// NewStorage creates new in-memory CDN metadata using the afero.NewMemMapFs() in memory file system
func NewStorage() (metadata.Storage, error) {
	const op errors.Op = "mem.NewStorage"
	l.Lock()
	defer l.Unlock()

	if memStorage != nil {
		return memStorage, nil
	}

	memFs := afero.NewMemMapFs()
	tmpDir, err := afero.TempDir(memFs, "", "")
	if err != nil {
		return nil, errors.E(op, fmt.Errorf("could not create temp dir for 'In Memory' CDN metadata (%s)", err))
	}

	memStorage = fs.NewStorage(memFs, tmpDir)
	return memStorage, nil
}
