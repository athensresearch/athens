package gcp

import (
	"context"

	"github.com/gomods/athens/pkg/config"
)

// Exists implements the (./pkg/storage).Checker interface
// returning true if the module at version exists in storage
func (s *Storage) Exists(ctx context.Context, module, version string) bool {
	return s.bucket.Exists(ctx, config.PackageVersionedName(module, version, ".mod"))
}
