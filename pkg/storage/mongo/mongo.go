package mongo

import (
	"github.com/globalsign/mgo"
)

type MongoModuleStore struct {
	s   *mgo.Session
	d   string // database
	c   string // collection
	url string
}

// NewMongoStorage  returns an unconnected Mongo Module Storage
// that satisfies the Storage interface.  You must call
// Connect() on the returned store before using it.
func NewMongoStorage(url string) *MongoModuleStore {
	return &MongoModuleStore{url: url}
}

func (m *MongoModuleStore) Connect() error {
	s, err := mgo.Dial(m.url)
	if err != nil {
		return err
	}
	m.s = s

	// TODO: database and collection as env vars, or params to New()? together with user/mongo
	m.d = "athens"
	m.c = "modules"

	index := mgo.Index{
		Key:        []string{"base_url", "module", "version"},
		Unique:     true,
		DropDups:   true,
		Background: true,
		Sparse:     true,
	}
	c := m.s.DB(m.d).C(m.c)
	return c.EnsureIndex(index)
}
