package memory

import (
	"fmt"
	"sync"

	"github.com/gomods/athens/pkg/storage"
)

type entriesT struct {
	*sync.RWMutex
	// keys are baseURL + "/" + module. use the 'key' method to get these
	versions map[string][]*storage.Version
}

func (e *entriesT) key(baseURL, module string) string {
	return fmt.Sprintf("%s/%s", baseURL, module)
}

var entries = &entriesT{
	RWMutex:  new(sync.RWMutex),
	versions: map[string][]*storage.Version{},
}
