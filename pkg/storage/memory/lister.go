package memory

import (
	"github.com/arschles/vgoprox/pkg/storage"
)

type Lister struct{}

func (l *Lister) List(basePath, module string) ([]string, error) {
	key := entries.key(basePath, module)
	entries.RLock()
	defer entries.RUnlock()
	versions, ok := entries.versions[key]
	if !ok {
		return nil, storage.NotFoundErr{BasePath: basePath, Module: module}
	}
	ret := make([]string, len(versions))
	for i, version := range versions {
		ret[i] = version.RevInfo.Version
	}
	return ret, nil
}

func (l *Lister) All() ([]*storage.RevInfo, error) {
	ret := []*storage.RevInfo{}
	entries.RLock()
	defer entries.RUnlock()
	for _, versions := range entries.versions {
		for _, version := range versions {
			ret = append(ret, &version.RevInfo)
		}
	}
	return ret, nil
}
