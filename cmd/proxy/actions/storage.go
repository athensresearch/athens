package actions

import (
	"context"
	"fmt"
	"strings"

	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/fs"
	"github.com/gomods/athens/pkg/storage/gcp"
	"github.com/gomods/athens/pkg/storage/mem"
	"github.com/gomods/athens/pkg/storage/minio"
	"github.com/gomods/athens/pkg/storage/mongo"
	"github.com/gomods/athens/pkg/storage/rdbms"
	"github.com/spf13/afero"
)

// GetStorage returns storage backend based on env configuration
func GetStorage() (storage.BackendConnector, error) {
	// changing to mongo storage, memory seems buggy
	storageType := env.StorageTypeWithDefault("mongo")
	var storageRoot string
	var err error

	switch storageType {
	case "memory":
		return mem.NewStorage()
	case "mongo":
		storageRoot, err = env.MongoURI()
		if err != nil {
			return nil, err
		}
		return mongo.NewStorage(storageRoot), nil
	case "disk":
		storageRoot, err = env.DiskRoot()
		if err != nil {
			return nil, err
		}
		s, err := fs.NewStorage(storageRoot, afero.NewOsFs())
		if err != nil {
			return nil, fmt.Errorf("could not create new storage from os fs (%s)", err)
		}
		return storage.NoOpBackendConnector(s), nil
	case "postgres", "sqlite", "cockroach", "mysql":
		storageRoot, err = env.RdbmsName()
		if err != nil {
			return nil, err
		}
		return rdbms.NewRDBMSStorage(storageRoot), nil
	case "minio":
		endpoint, err := env.MinioEndpoint()
		if err != nil {
			return nil, err
		}
		accessKeyID, err := env.MinioAccessKeyID()
		if err != nil {
			return nil, err
		}
		secretAccessKey, err := env.MinioSecretAccessKey()
		if err != nil {
			return nil, err
		}
		bucketName := env.MinioBucketNameWithDefault("gomods")
		useSSL := true
		if useSSLVar := env.MinioSSLWithDefault("yes"); strings.ToLower(useSSLVar) == "no" {
			useSSL = false
		}
		s, err := minio.NewStorage(endpoint, accessKeyID, secretAccessKey, bucketName, useSSL)
		return storage.NoOpBackendConnector(s), err
	case "gcp":
		return gcp.New(context.Background())
	default:
		return nil, fmt.Errorf("storage type %s is unknown", storageType)
	}
}
