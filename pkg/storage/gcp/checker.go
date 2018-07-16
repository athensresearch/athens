package gcp

import (
	"context"
)

// Exists implements the (./pkg/storage).Checker interface
// returning true if the module at version exists in storage
func (s *Storage) Exists(module, version string) bool {
	ctx := context.Background()
	return s.bucket.Exists(ctx, module, version)
}
