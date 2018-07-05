package actions

import (
	"fmt"

	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/fs"
	"github.com/gomods/athens/pkg/storage/mem"
	"github.com/gomods/athens/pkg/storage/mongo"
	"github.com/gomods/athens/pkg/storage/rdbms"
	"github.com/spf13/afero"
)

// GetStorage returns storage.BackendConnector implementation
func GetStorage() (storage.BackendConnector, error) {
	storageType := env.StorageTypeWithDefault("memory")
	switch storageType {
	case "memory":
		return mem.NewStorage()
	case "disk":
		rootLocation, err := env.DiskRoot()
		if err != nil {
			return nil, err
		}
		s, err := fs.NewStorage(rootLocation, afero.NewOsFs())
		if err != nil {
			return nil, fmt.Errorf("could not create new storage from os fs (%s)", err)
		}
		return storage.NoOpBackendConnector(s), nil
	case "mongo":
		mongoURI, err := env.MongoURI()
		if err != nil {
			return nil, err
		}
		return mongo.NewStorage(mongoURI), nil
	case "postgres", "sqlite", "cockroach", "mysql":
		connectionName, err := env.RdbmsName()
		if err != nil {
			return nil, err
		}
		return rdbms.NewRDBMSStorage(connectionName), nil
	default:
		return nil, fmt.Errorf("storage type %s is unknown", storageType)
	}
}
