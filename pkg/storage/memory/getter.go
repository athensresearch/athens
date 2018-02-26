package memory

import (
	"github.com/arschles/vgoprox/pkg/storage"
)

type Getter struct{}

func (v *Getter) Get(baseURL, module, vsn string) (*storage.Version, error) {
	entries.RLock()
	defer entries.RUnlock()
	key := entries.key(baseURL, module)
	versions := entries.versions[key]
	for _, version := range versions {
		if version.RevInfo.Version == vsn {
			return version, nil
		}
	}
	return nil, &storage.ErrVersionNotFound{
		BasePath: baseURL,
		Module:   module,
		Version:  vsn,
	}

}
