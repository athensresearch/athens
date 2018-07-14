package gcp

import (
	"context"

	"github.com/gomods/athens/pkg/config"
)

// Exists implements the (./pkg/storage).Checker interface
// returning true if the module at version exists in storage
func (s *Storage) Exists(module, version string) bool {
	ctx := context.Background()
	modHandle := s.bucket.Object(config.PackageVersionedName(module, version, "mod"))
	_, err := modHandle.Attrs(ctx)
	// Unless the signature changes for Exists just say false on any error.
	// Attrs will error with not found if it doesn't exist.
	if err != nil {
		return false
	}
	return true
}
