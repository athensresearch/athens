package mongo

import (
	"testing"

	"github.com/gomods/athens/pkg/eventlog"
	"github.com/stretchr/testify/suite"
)

type MongoRegistryTests struct {
	suite.Suite
	registry *Registry
}

func TestRegistry(t *testing.T) {
	suite.Run(t, new(MongoRegistryTests))
}

func (m *MongoRegistryTests) SetupTest() {
	store, err := NewRegistry("mongodb://127.0.0.1:27017")
	if err != nil {
		panic(err)
	}

	store.Connect()

	store.s.DB(store.d).C(store.c).RemoveAll(nil)
	m.registry = store
}

func (m *MongoRegistryTests) TestSetReadRoundTrip() {
	r := m.Require()
	err := m.registry.SetPointer("deployment1", "location1")
	r.NoError(err)
	gotten, err := m.registry.LookupPointer("deployment1")
	r.NoError(err)
	r.Equal("location1", gotten)

	err = m.registry.SetPointer("deployment1", "location2")
	r.NoError(err)
	gotten, err = m.registry.LookupPointer("deployment1")
	r.NoError(err)
	r.Equal("location2", gotten)

	gotten, err = m.registry.LookupPointer("doesnt-exist")
	r.Equal(eventlog.ErrDeploymentNotFound, err)
}

func (m *MongoRegistryTests) TestNewRegistry() {
	r := m.Require()
	url := "mongodb://127.0.0.1:27017"
	registry, err := NewRegistry(url)
	r.NoError(err)
	err = registry.Connect()
	r.NoError(err)

	r.NotNil(registry.c)
	r.NotNil(registry.d)
	r.NotNil(registry.s)
	r.Equal(url, registry.url)
}
