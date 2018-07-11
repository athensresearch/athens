package rdbms

import (
	"github.com/gobuffalo/pop"
)

// ModuleStore represents a rdbms(postgres, mysql, sqlite, cockroachdb) backed storage backend.
type ModuleStore struct {
	conn           *pop.Connection
	connectionName string // settings name from database.yml
}

// NewRDBMSStorage  returns an unconnected RDBMS Module Storage
// that satisfies the Storage interface. You must call
// Connect() on the returned store before using it.
// connectionName
func NewRDBMSStorage(connectionName string) *ModuleStore {
	return &ModuleStore{
		connectionName: connectionName,
	}
}

// NewRDBMSStorageWithConn  returns a connected RDBMS Module Storage
// that satisfies the Storage interface. You must call
// Connect() on the returned store before using it.
// connectionName
func NewRDBMSStorageWithConn(connection *pop.Connection) *ModuleStore {
	return &ModuleStore{
		conn: connection,
	}
}

// Connect creates connection to rdmbs backend.
func (r *ModuleStore) Connect() error {
	c, err := pop.Connect(r.connectionName)
	if err != nil {
		return err
	}
	r.conn = c
	return nil
}
