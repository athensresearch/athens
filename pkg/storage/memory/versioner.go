package memory

import (
	"github.com/arschles/vgoprox/pkg/storage"
)

type Versioner struct{}

func (v *Versioner) Info(baseURL, module, vsn string) (*storage.RevInfo, error) {
	entries.RLock()
	defer entries.RUnlock()
	key := entries.key(baseURL, module)
	versions := entries.versions[key]
	for _, version := range versions {
		if version.info.Version == vsn {
			return &version.info, nil
		}
	}
	return nil, &storage.ErrVersionNotFound{
		NotFoundErr: storage.NotFoundErr{
			BasePath: baseURL,
			Module:   module,
		},
		Version: vsn,
	}

}
