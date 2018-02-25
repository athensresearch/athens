package memory

import (
	"time"

	"github.com/arschles/vgoprox/pkg/storage"
)

type Saver struct{}

func (s *Saver) Save(baseURL, module, vsn string, mod, zip []byte) error {
	newVsn := &version{
		info: storage.RevInfo{
			Version: vsn,
			Short:   vsn,
			Time:    time.Now(),
			Name:    vsn,
		},
		mod: mod,
		zip: zip,
	}
	entries.Lock()
	defer entries.Unlock()
	key := entries.key(baseURL, module)
	existing := entries.versions[key]
	existing = append(existing, newVsn)
	return nil
}
