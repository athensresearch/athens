package storage

import "context"

// Getter gets module metadata and its source from underlying storage
type Getter interface {
	// Get must return ErrNotFound if the coordinates are not found
	Get(ctx context.Context, module, vsn string) (*Version, error)
}
