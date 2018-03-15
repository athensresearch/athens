package memory

import (
	"github.com/gomods/athens/pkg/storage"
)

func (v *getterSaverImpl) Get(baseURL, module, vsn string) (*storage.Version, error) {
	v.RLock()
	defer v.RUnlock()
	key := v.key(baseURL, module)
	versions := v.versions[key]
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
