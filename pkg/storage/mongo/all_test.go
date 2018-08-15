package mongo

import (
	"testing"

	"github.com/gomods/athens/pkg/storage"
	"github.com/stretchr/testify/suite"
)

type MongoTests struct {
	suite.Suite
	storage storage.Backend
}

func (d *MongoTests) SetupTest() {
	store, err := NewStorage("mongodb://127.0.0.1:27017")

	d.NoError(err)

	store.s.DB(store.d).C(store.c).RemoveAll(nil)
	d.storage = store
}

func TestDiskStorage(t *testing.T) {
	suite.Run(t, new(MongoTests))
}
