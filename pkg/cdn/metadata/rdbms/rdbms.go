package rdbms

import (
	"github.com/gobuffalo/pop"
)

// MetadataStore represents a rdbms(postgres, mysql, sqlite, cockroachdb) backed metadata store.
type MetadataStore struct {
	conn           *pop.Connection
	connectionName string // settings name from database.yml
}

// NewStorage  returns an unconnected RDBMS Metadata Storage
// that satisfies the Getter and Setter interfaces. You must call
// Connect() on the returned store before using it.
// connectionName
func NewStorage(connectionName string) *MetadataStore {
	return &MetadataStore{
		connectionName: connectionName,
	}
}

// Connect creates connection to rdmbs backend.
func (r *MetadataStore) Connect() error {
	c, err := pop.Connect(r.connectionName)
	if err != nil {
		return err
	}
	r.conn = c
	return nil
}
