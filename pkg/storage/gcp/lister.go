package gcp

import (
	"context"
)

// List implements the (./pkg/storage).Lister interface
// It returns a list of versions, if any, for a given module
func (s *Storage) List(module string) ([]string, error) {
	ctx := context.Background()
	return s.bucket.ListVersions(ctx, module)
}
