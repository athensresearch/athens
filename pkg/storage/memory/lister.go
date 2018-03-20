package memory

import (
	"github.com/gomods/athens/pkg/storage"
)

func (v *getterSaverImpl) List(basePath, module string) ([]string, error) {
	key := v.key(basePath, module)
	v.RLock()
	defer v.RUnlock()
	versions, ok := v.versions[key]
	if !ok {
		return nil, storage.ErrNotFound{BasePath: basePath, Module: module}
	}
	ret := make([]string, len(versions))
	for i, version := range versions {
		ret[i] = version.RevInfo.Version
	}
	return ret, nil
}
