package gcp

import (
	"context"
)

// Delete implements the (./pkg/storage).Deleter interface and
// removes a version of a module from storage. Returning ErrNotFound
// if the version does not exist.
func (s *Storage) Delete(module, version string) error {
	ctx := context.Background()
	return s.bucket.Delete(ctx, module, version)
}
