package disk

import (
	"io/ioutil"
)

func (l *storageImpl) List(basePath, module string) ([]string, error) {
	loc := l.moduleDiskLocation(basePath, module)
	fileInfos, err := ioutil.ReadDir(loc)
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
