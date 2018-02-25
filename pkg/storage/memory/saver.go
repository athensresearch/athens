package memory

import (
	"time"

	"github.com/arschles/vgoprox/pkg/storage"
)

type Saver struct{}

func (s *Saver) Save(baseURL, module, vsn string, mod, zip []byte) error {
	newVsn := &storage.Version{
		RevInfo: storage.RevInfo{
			Version: vsn,
			Short:   vsn,
			Time:    time.Now(),
			Name:    vsn,
		},
		Mod: mod,
		Zip: zip,
	}
	entries.Lock()
	defer entries.Unlock()
	key := entries.key(baseURL, module)
	entries.versions[key] = append(entries.versions[key], newVsn)
	return nil
}
