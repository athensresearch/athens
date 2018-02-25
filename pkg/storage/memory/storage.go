package memory

import (
	"fmt"
	"sync"

	"github.com/arschles/vgoprox/pkg/storage"
)

type version struct {
	info storage.RevInfo
	mod  []byte
	zip  []byte
}

type entriesT struct {
	*sync.RWMutex
	// keys are baseURL + "/" + module. use the 'key' method to get these
	versions map[string][]*version
}

func (e *entriesT) key(baseURL, module string) string {
	return fmt.Sprintf("%s/%s", baseURL, module)
}

var entries = &entriesT{
	RWMutex:  new(sync.RWMutex),
	versions: map[string][]*version{},
}
