package actions

import (
	"fmt"
	"strings"

	"github.com/gobuffalo/envy"
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/fs"
	"github.com/gomods/athens/pkg/storage/mem"
	"github.com/gomods/athens/pkg/storage/minio"
	"github.com/gomods/athens/pkg/storage/mongo"
	"github.com/gomods/athens/pkg/storage/rdbms"
	"github.com/spf13/afero"
)

// GetStorage returns storage backend based on env configuration
func GetStorage() (storage.BackendConnector, error) {
	// changing to mongo storage, memory seems buggy
	storageType := envy.Get("ATHENS_STORAGE_TYPE", "mongo")
	var storageRoot string
	var err error

	switch storageType {
	case "memory":
		return mem.NewStorage()
	case "mongo":
		storageRoot, err = envy.MustGet("ATHENS_MONGO_STORAGE_URL")
		if err != nil {
			return nil, fmt.Errorf("missing mongo URL (%s)", err)
		}
		return mongo.NewStorage(storageRoot), nil
	case "disk":
		storageRoot, err = envy.MustGet("ATHENS_DISK_STORAGE_ROOT")
		if err != nil {
			return nil, fmt.Errorf("missing disk storage root (%s)", err)
		}
		s := fs.NewStorage(storageRoot, afero.NewOsFs())
		return storage.NoOpBackendConnector(s), nil
	case "postgres", "sqlite", "cockroach", "mysql":
		storageRoot, err = envy.MustGet("ATHENS_RDBMS_STORAGE_NAME")
		if err != nil {
			return nil, fmt.Errorf("missing rdbms connectionName (%s)", err)
		}
		return rdbms.NewRDBMSStorage(storageRoot), nil
	case "minio":
		endpoint, err := envy.MustGet("ATHENS_MINIO_ENDPOINT")
		if err != nil {
			return nil, fmt.Errorf("missing minio endpoint (%s)", err)
		}
		accessKeyID, err := envy.MustGet("ATHENS_MINIO_ACCESS_KEY_ID")
		if err != nil {
			return nil, fmt.Errorf("missing minio access key ID (%s)", err)
		}
		secretAccessKey, err := envy.MustGet("ATHENS_MINIO_SECRET_ACCESS_KEY")
		if err != nil {
			return nil, fmt.Errorf("missing minio secret access key (%s)", err)
		}
		bucketName := envy.Get("ATHENS_MINIO_BUCKET_NAME", "gomods")
		useSSL := true
		if useSSLVar := envy.Get("ATHENS_MINIO_USE_SSL", "yes"); strings.ToLower(useSSLVar) == "no" {
			useSSL = false
		}
		s, err := minio.NewStorage(endpoint, accessKeyID, secretAccessKey, bucketName, useSSL)
		return storage.NoOpBackendConnector(s), err
	default:
		return nil, fmt.Errorf("storage type %s is unknown", storageType)
	}
}
