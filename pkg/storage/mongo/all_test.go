package mongo

import (
	"testing"

	"github.com/gomods/athens/pkg/storage"
	"github.com/stretchr/testify/suite"
)

const (
	module  = "testmodule"
	version = "v1.0.0"
)

var (
	// TODO: put these values inside of the suite, and generate longer values.
	// This should help catch edge cases, like https://github.com/gomods/athens/issues/38
	//
	// Also, consider doing something similar to what testing/quick does
	// with the Generator interface (https://godoc.org/testing/quick#Generator).
	// The rough, simplified idea would be to run a single test case multiple
	// times over different (increasing) values.
	mod  = []byte("123")
	zip  = []byte("456")
	info = []byte("789")
)

type MongoTests struct {
	suite.Suite
	storage storage.BackendConnector
}

func (d *MongoTests) SetupTest() {
	store := NewStorage("mongodb://127.0.0.1:27017")
	store.Connect()

	store.s.DB(store.d).C(store.c).RemoveAll(nil)
	d.storage = store
}

func TestDiskStorage(t *testing.T) {
	suite.Run(t, new(MongoTests))
}
