package module

import (
	"github.com/gomods/athens/pkg/storage"
)

// Ref points to a module somewhere
type Ref interface {
	// Read reads the module into memory and returns it. Notice that the Zip field on the returned
	// storage.Version is an io.ReadCloser, so make sure to call Close on it after you're done
	// with it.
	Read() (*storage.Version, error)
}
