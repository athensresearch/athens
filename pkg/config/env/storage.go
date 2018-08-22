package env

import (
	"fmt"

	"github.com/gobuffalo/envy"
)

// DiskRoot returns Athens Mongo Disk Root folder defined by ATHENS_DISK_STORAGE_ROOT
func DiskRoot() (string, error) {
	env, err := envy.MustGet("ATHENS_DISK_STORAGE_ROOT")
	if err != nil {
		return "", fmt.Errorf("missing disk root: %s", err)
	}

	return env, nil
}

// StorageType returns storage type used by Athens with error if env is not set.
// Possible values are memory, disk, mongo
func StorageType() (string, error) {
	return envy.MustGet("ATHENS_STORAGE_TYPE")
}

// StorageTypeWithDefault returns storage type used by Athens with default value if env is not set.
// Possible values are memory, disk, mongo
func StorageTypeWithDefault(value string) string {
	return envy.Get("ATHENS_STORAGE_TYPE", value)
}
