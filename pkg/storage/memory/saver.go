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
	existingVersionsSlice := entries.versions[key]
	found := false
	for _, version := range existingVersionsSlice {
		if version.RevInfo.Version == vsn {
			return storage.ErrVersionAlreadyExists{BaseURL: baseURL, Module: module, Version: vsn}
		}
	}
	newVersionsSlice := append(existingVersionsSlice, newVsn)
	entries.versions[key] = newVersionsSlice
	return nil
}
