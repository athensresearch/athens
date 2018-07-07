package storage

import (
	"context"
	"io"
)

// Saver saves module metadata and its source to underlying storage
type Saver interface {
	Save(ctx context.Context, module, version string, mod []byte, zip io.Reader, info []byte) error
}
