package actions

import (
	"fmt"

	"github.com/gobuffalo/envy"
	"github.com/gomods/athens/pkg/storage"
	"github.com/gomods/athens/pkg/storage/disk"
	"github.com/gomods/athens/pkg/storage/memory"
	"github.com/gomods/athens/pkg/storage/mongo"
)

func newStorage() (storage.Storage, error) {
	storageType := envy.Get("ATHENS_STORAGE_TYPE", "memory")
	switch storageType {
	case "memory":
		return memory.NewMemoryStorage(), nil
	case "disk":
		rootLocation, err := envy.MustGet("ATHENS_DISK_STORAGE_ROOT")
		if err != nil {
			return nil, fmt.Errorf("missing disk storage root (%s)", err)
		}
		return disk.NewStorage(rootLocation), nil
	case "mongo":
		mongoURI, err := envy.MustGet("ATHENS_MONGO_STORAGE_URL")
		if err != nil {
			return nil, fmt.Errorf("missing mongo URL (%s)", err)
		}
		return mongo.NewMongoStorage(mongoURI), nil
	default:
		return nil, fmt.Errorf("storage type %s is unknown", storageType)
	}
}
