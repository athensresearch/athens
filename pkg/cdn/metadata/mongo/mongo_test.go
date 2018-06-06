package mongo

import (
	"testing"

	"github.com/gomods/athens/pkg/cdn/metadata"
	"github.com/gomods/athens/pkg/fixtures"
	"github.com/stretchr/testify/suite"
)

const (
	module      = "testmodule"
	redirectURL = "https://mycdn.com/gomods.io/my/testmodule"
)

type MongoTests struct {
	*fixtures.Mongo
	storage metadata.StorageConnector
}

func TestMongo(t *testing.T) {
	suite.Run(t, &MongoTests{Mongo: fixtures.NewMongo(fixtures.DefaultMongoURL)})
}

func (m *MongoTests) SetupTest() {
	m.Mongo.SetupTest()
	store := NewStorage(fixtures.DefaultMongoURL, m.Mongo.DBName)
	store.Connect()
	m.storage = store
}

func (m *MongoTests) TestGetSaveListRoundTrip() {
	r := m.Require()
	err := m.storage.Save(module, redirectURL)
	r.NoError(err)

	gotten, err := m.storage.Get(module)
	r.NoError(err)
	r.Equal(gotten, redirectURL)
}

func (m *MongoTests) TestNewMongoStorage() {
	r := m.Require()
	getterSaver := NewStorage(fixtures.DefaultMongoURL, m.Mongo.DBName)
	getterSaver.Connect()

	r.NotNil(getterSaver.col)
	r.NotNil(getterSaver.db)
	r.NotNil(getterSaver.session)
	r.Equal(getterSaver.url, fixtures.DefaultMongoURL)
}
