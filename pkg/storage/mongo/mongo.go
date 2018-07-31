package mongo

import (
	"strings"

	"github.com/globalsign/mgo"
)

// ModuleStore represents a mongo backed storage backend.
type ModuleStore struct {
	s   *mgo.Session
	d   string // database
	c   string // collection
	url string
}

// NewStorage returns an unconnected Mongo backed storage
// that satisfies the Backend interface.  You must call
// Connect() on the returned store before using it.
func NewStorage(url string) *ModuleStore {
	return &ModuleStore{url: url}
}

// Connect conntect the the newly created mongo backend.
func (m *ModuleStore) Connect() error {
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

func (m *ModuleStore) gridFileName(mod, ver string) string {
	return strings.Replace(mod, "/", "_", -1) + "_" + ver + ".zip"
}
