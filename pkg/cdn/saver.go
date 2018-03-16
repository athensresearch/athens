package cdn

import (
	"github.com/gomods/athens/pkg/storage"
)

// Saver saves a module metadata & storage to its underlying storage
type Saver interface {
	Save(module, version *storage.Version) error
}
