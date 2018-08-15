package rdbms

import (
	"github.com/gobuffalo/pop"
	"github.com/gomods/athens/pkg/errors"
)

// ModuleStore represents a rdbms(postgres, mysql, sqlite, cockroachdb) backed storage backend.
type ModuleStore struct {
	conn           *pop.Connection
	connectionName string // settings name from database.yml
}

// NewRDBMSStorage  returns a connected RDBMS Module Storage
// that satisfies the Storage interface.
func NewRDBMSStorage(connectionName string) (*ModuleStore, error) {
	const op errors.Op = "rdbms.NewRDBMSStorage"

	ms := &ModuleStore{
		connectionName: connectionName,
	}
	err := ms.connect()
	if err != nil {
		return nil, errors.E(op, err)
	}
	return ms, nil
}

// NewRDBMSStorageWithConn  returns a connected RDBMS Module Storage
// that satisfies the Storage interface.
func NewRDBMSStorageWithConn(connection *pop.Connection) *ModuleStore {
	ms := &ModuleStore{
		conn: connection,
	}
	return ms
}

func (r *ModuleStore) connect() error {
	c, err := pop.Connect(r.connectionName)
	if err != nil {
		return err
	}
	r.conn = c
	return nil
}
