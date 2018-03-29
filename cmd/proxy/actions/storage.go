package actions

import (
	"fmt"

	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/fs"
	"github.com/gomods/athens/pkg/storage/mongo"
	"github.com/gomods/athens/pkg/storage/rdbms"
	"github.com/spf13/afero"
)

func newStorage(storageType, storageLocation string) (storage.Backend, error) {
	switch storageType {
	case "memory":
		memFs := afero.NewMemMapFs()
		tmpDir, err := afero.TempDir(memFs, "inmem", "")
		if err != nil {
			return nil, fmt.Errorf("could not create temp dir for 'In Memory' storage (%s)", err)
		}
		return fs.NewStorage(tmpDir, memFs), nil
	case "disk":
		return fs.NewStorage(storageLocation, afero.NewOsFs()), nil
	case "mongo":
		return mongo.NewStorage(storageLocation), nil
	case "postgres", "sqlite", "cockroach", "mysql":
		return rdbms.NewRDBMSStorage(storageLocation), nil
	default:
		return nil, fmt.Errorf("storage type %s is unknown", storageType)
	}
}
