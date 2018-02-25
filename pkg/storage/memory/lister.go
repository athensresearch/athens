package memory

import (
	"fmt"

	"github.com/pkg/errors"
)

type Lister struct{}

func (l *Lister) List(baseURL, module string) ([]string, error) {
	key := entries.key(baseURL, module)
	entries.RLock()
	defer entries.RUnlock()
	versions, ok := entries.versions[key]
	if !ok {
		return nil, errors.WithStack(fmt.Errorf("no entry %s", key))
	}
	ret := make([]string, len(versions))
	for i, version := range versions {
		ret[i] = version.info.Version
	}
	return ret, nil
}
