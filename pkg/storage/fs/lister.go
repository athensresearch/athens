package fs

import (
	"github.com/spf13/afero"
)

func (l *storageImpl) List(basePath, module string) ([]string, error) {
	loc := l.moduleLocation(basePath, module)
	fileInfos, err := afero.ReadDir(l.filesystem, loc)
	if err != nil {
		return nil, err
	}
	ret := []string{}
	for _, fileInfo := range fileInfos {
		if fileInfo.IsDir() {
			ret = append(ret, fileInfo.Name())
		}
	}
	return ret, nil
}
