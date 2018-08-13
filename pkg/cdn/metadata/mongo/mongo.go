package mongo

import (
	"github.com/globalsign/mgo"
	"github.com/gomods/athens/pkg/errors"
)

// MetadataStore represents a Mongo backed metadata store.
type MetadataStore struct {
	session *mgo.Session
	db      string
	col     string
	url     string
}

// NewStorage returns an unconnected Mongo backed storage
// that satisfies the Storage interface.  You must call
// Connect() on the returned store before using it.
func NewStorage(url, dbName string) *MetadataStore {
	return &MetadataStore{url: url, db: dbName}
}

// Connect conntect the the newly created mongo backend.
func (m *MetadataStore) Connect() error {
	const op errors.Op = "mongoCDN.Connect"
	s, err := mgo.Dial(m.url)
	if err != nil {
		return errors.E(op, err)
	}
	m.session = s

	m.col = "cdn_metadata"

	index := mgo.Index{
		Key:        []string{"base_url", "module"},
		Unique:     true,
		DropDups:   true,
		Background: true,
		Sparse:     true,
	}
	c := m.session.DB(m.db).C(m.col)
	err = c.EnsureIndex(index)
	if err != nil {
		return errors.E(op, err)
	}
	return nil
}
