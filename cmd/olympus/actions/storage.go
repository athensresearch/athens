package actions

import (
	"fmt"

	"github.com/gobuffalo/envy"
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/fs"
	"github.com/gomods/athens/pkg/storage/mongo"
	"github.com/gomods/athens/pkg/storage/rdbms"
	"github.com/spf13/afero"
)

func newStorage() (storage.Backend, error) {
	storageType := envy.Get("ATHENS_STORAGE_TYPE", "memory")
	switch storageType {
	case "memory":
		memFs := afero.NewMemMapFs()
		tmpDir, err := afero.TempDir(memFs, "inmem", "")
		if err != nil {
			return nil, fmt.Errorf("could not create temp dir for 'In Memory' storage (%s)", err)
		}
		return fs.NewStorage(tmpDir, memFs), nil
	case "disk":
		rootLocation, err := envy.MustGet("ATHENS_DISK_STORAGE_ROOT")
		if err != nil {
			return nil, fmt.Errorf("missing disk storage root (%s)", err)
		}
		return fs.NewStorage(rootLocation, afero.NewOsFs()), nil
	case "mongo":
		mongoURI, err := envy.MustGet("ATHENS_MONGO_STORAGE_URL")
		if err != nil {
			return nil, fmt.Errorf("missing mongo URL (%s)", err)
		}
		return mongo.NewStorage(mongoURI), nil
	case "postgres", "sqlite", "cockroach", "mysql":
		connectionName, err := envy.MustGet("ATHENS_RDBMS_STORAGE_NAME")
		if err != nil {
			return nil, fmt.Errorf("missing rdbms connectionName (%s)", err)
		}
		return rdbms.NewRDBMSStorage(connectionName), nil
	default:
		return nil, fmt.Errorf("storage type %s is unknown", storageType)
	}
}
