package storage

import "context"

// Saver saves module metadata and its source to underlying storage
type Saver interface {
	Save(ctx context.Context, module, version string, mod, zip, info []byte) error
}
