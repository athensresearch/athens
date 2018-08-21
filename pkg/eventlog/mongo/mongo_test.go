package mongo

import (
	"testing"
	"time"

	"github.com/gomods/athens/pkg/eventlog"
	"github.com/stretchr/testify/suite"
)

type MongoTests struct {
	suite.Suite
	log *Log
}

func TestMongo(t *testing.T) {
	suite.Run(t, new(MongoTests))
}

func (m *MongoTests) SetupTest() {
	store, err := NewLog("mongodb://127.0.0.1:27017", "")
	if err != nil {
		panic(err)
	}

	store.Connect()

	store.s.DB(store.db).C(store.col).RemoveAll(nil)
	m.log = store
}

func (m *MongoTests) TestRead() {
	r := m.Require()
	versions := []string{"v1.0.0", "v1.1.0", "v1.2.0"}
	for _, version := range versions {
		_, err := m.log.Append(eventlog.Event{Module: "m1", Version: version, Time: time.Now()})
		r.NoError(err)
	}

	retVersions, err := m.log.Read()
	r.NoError(err)
	r.Equal(versions[0], retVersions[0].Version)
	r.Equal(versions[1], retVersions[1].Version)
	r.Equal(versions[2], retVersions[2].Version)
}

func (m *MongoTests) TestReadFrom() {
	r := m.Require()
	versions := []string{"v1.0.0", "v1.1.0", "v1.2.0"}
	pointers := make(map[string]string)
	for _, version := range versions {
		p, _ := m.log.Append(eventlog.Event{Module: "m1", Version: version, Time: time.Now()})
		pointers[version] = p
	}

	retVersions, err := m.log.ReadFrom(pointers[versions[0]])
	r.NoError(err)
	r.Equal(versions[1], retVersions[0].Version)
	r.Equal(versions[2], retVersions[1].Version)

	retVersions, err = m.log.ReadFrom(pointers[versions[1]])
	r.NoError(err)
	r.Equal(versions[2], retVersions[0].Version)

	retVersions, err = m.log.ReadFrom(pointers[versions[2]])
	r.NoError(err)
	r.Equal(0, len(retVersions))
}

func (m *MongoTests) TestClear() {
	r := m.Require()
	versions := []string{"v1.0.0", "v1.1.0", "v1.2.0"}
	for _, version := range versions {
		m.log.Append(eventlog.Event{Module: "m1", Version: version, Time: time.Now()})
	}

	retVersions, err := m.log.Read()
	r.NoError(err)
	r.Equal(3, len(retVersions))

	err = m.log.Clear("")
	r.NoError(err)

	retVersions, err = m.log.Read()
	r.NoError(err)
	r.Equal(0, len(retVersions))
}

func (m *MongoTests) TestClearFrom() {
	r := m.Require()
	versions := []string{"v1.0.0", "v1.1.0", "v1.2.0"}
	pointers := make(map[string]string)
	for _, version := range versions {
		p, _ := m.log.Append(eventlog.Event{Module: "m1", Version: version, Time: time.Now()})
		pointers[version] = p
	}

	retVersions, err := m.log.Read()
	r.NoError(err)
	r.Equal(3, len(retVersions))

	err = m.log.Clear(pointers[versions[1]])
	r.NoError(err)

	retVersions, err = m.log.Read()
	r.NoError(err)
	r.Equal(1, len(retVersions))
	r.Equal(versions[2], retVersions[0].Version)
}
