package storage

import (
	"context"
	"io"
)

// Getter gets module metadata and its source from underlying storage
type Getter interface {
	Info(ctx context.Context, module, vsn string) ([]byte, error)
	GoMod(ctx context.Context, module, vsn string) ([]byte, error)
	Zip(ctx context.Context, module, vsn string) (io.ReadCloser, error)
}
