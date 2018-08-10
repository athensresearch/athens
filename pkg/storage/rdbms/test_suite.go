package rdbms

import (
	"github.com/gobuffalo/suite"
	"github.com/gomods/athens/pkg/storage"
)

// TestSuite implements TestSuite interface
type TestSuite struct {
	*suite.Model
	storage storage.Backend
}

// NewTestSuite creates a common test suite
func NewTestSuite(model *suite.Model) (storage.TestSuite, error) {
	conn := model.DB
	rdbmsStore := NewRDBMSStorageWithConn(conn)

	return &TestSuite{
		storage: rdbmsStore,
		Model:   model,
	}, nil
}

// Storage retrieves initialized storage backend
func (ts *TestSuite) Storage() storage.Backend {
	return ts.storage
}

// StorageHumanReadableName retrieves readable identifier of the storage
func (ts *TestSuite) StorageHumanReadableName() string {
	return "Rdbms"
}

// Cleanup tears down test
func (ts *TestSuite) Cleanup() error {
	return nil
}
