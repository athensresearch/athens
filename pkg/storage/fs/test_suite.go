package fs

import (
	"github.com/gobuffalo/suite"
	"github.com/gomods/athens/pkg/storage"
	"github.com/spf13/afero"
)

// TestSuite implements storage.TestSuite interface
type TestSuite struct {
	*suite.Model
	storage storage.Backend
	fs      afero.Fs
	rootDir string
}

// NewTestSuite creates a common test suite
func NewTestSuite(model *suite.Model) (storage.TestSuite, error) {
	memFs := afero.NewOsFs()
	r, err := afero.TempDir(memFs, "", "athens-fs-storage-tests")
	if err != nil {
		return nil, err
	}

	fsStore, err := NewStorage(r, memFs)
	if err != nil {
		return nil, err
	}

	return &TestSuite{
		Model:   model,
		fs:      memFs,
		rootDir: r,
		storage: fsStore,
	}, nil
}

// Storage retrieves initialized storage backend
func (ts *TestSuite) Storage() storage.Backend {
	return ts.storage
}

// StorageHumanReadableName retrieves readable identifier of the storage
func (ts *TestSuite) StorageHumanReadableName() string {
	return "FileSystem"
}

// Cleanup tears down test
func (ts *TestSuite) Cleanup() error {
	return ts.fs.RemoveAll(ts.rootDir)
}
