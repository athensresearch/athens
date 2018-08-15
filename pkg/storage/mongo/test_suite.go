package mongo

import (
	"fmt"

	"github.com/globalsign/mgo"
	"github.com/gobuffalo/suite"
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/storage"
)

// TestSuite implements storage.TestSuite interface
type TestSuite struct {
	*suite.Model
	storage *ModuleStore
}

// NewTestSuite creates a common test suite
func NewTestSuite(model *suite.Model) (storage.TestSuite, error) {
	ms, err := newTestStore()
	if err != nil {
		return nil, err
	}
	return &TestSuite{
		storage: ms,
		Model:   model,
	}, err
}

func newTestStore() (*ModuleStore, error) {
	muri, err := env.MongoURI()
	if err != nil {
		return nil, err
	}

	mongoStore, err := NewStorage(muri)
	if err != nil {
		return nil, fmt.Errorf("Not able to connect to mongo storage")
	}

	return mongoStore, nil
}

// Storage retrieves initialized storage backend
func (ts *TestSuite) Storage() storage.Backend {
	return ts.storage
}

// StorageHumanReadableName retrieves readable identifier of the storage
func (ts *TestSuite) StorageHumanReadableName() string {
	return "Mongo"
}

// Cleanup tears down test
func (ts *TestSuite) Cleanup() error {
	muri, err := env.MongoURI()
	if err != nil {
		return err
	}
	timeout := env.MongoConnectionTimeoutSecWithDefault(1)
	s, err := mgo.DialWithTimeout(muri, timeout)
	defer s.Close()
	if err != nil {
		return err
	}
	return s.DB("athens").C("modules").DropCollection()
}
