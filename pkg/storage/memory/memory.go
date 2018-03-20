package memory

import (
	"fmt"
	"sync"

	"github.com/gomods/athens/pkg/storage"
)

type getterSaverImpl struct {
	*sync.RWMutex
	versions map[string][]*storage.Version
}

func (e *getterSaverImpl) key(baseURL, module string) string {
	return fmt.Sprintf("%s/%s", baseURL, module)
}

// NewMemoryStorage returns a new ListerSaver implementation that stores
// everything in memory
func NewMemoryStorage() storage.Storage {
	return &getterSaverImpl{
		RWMutex:  new(sync.RWMutex),
		versions: make(map[string][]*storage.Version),
	}
}
