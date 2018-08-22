package gcp

import (
	"context"
	"io"
)

// Bucket provides file operations for a Google Cloud Storage resource.
type Bucket interface {
	// Delete removes the file
	Delete(ctx context.Context, path string) error
	// Open returns a reader for a path and any error
	Open(ctx context.Context, path string) (io.ReadCloser, error)
	// Write returns a new writer for a path
	// This writer will overwrite any existing file stored at the same path
	Write(ctx context.Context, path string) io.WriteCloser
	// List returns a slice of paths for a prefix and any error
	List(ctx context.Context, prefix string) ([]string, error)
	// Exists returns true if the file exists
	Exists(ctx context.Context, path string) (bool, error)
}
