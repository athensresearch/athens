package minio

import (
	"sort"
	"strings"
)

func (l *storageImpl) List(module string) ([]string, error) {
	dict := make(map[string]struct{})

	doneCh := make(chan struct{})
	defer close(doneCh)
	searchPrefix := module + "/"
	objectCh := l.minioClient.ListObjectsV2(l.bucketName, searchPrefix, false, doneCh)
	for object := range objectCh {
		if object.Err != nil {
			return nil, object.Err
		}
		parts := strings.Split(object.Key, "/")
		ver := parts[len(parts)-2]
		if _, ok := dict[ver]; !ok {
			dict[ver] = struct{}{}
		}
	}
	ret := []string{}
	for ver := range dict {
		ret = append(ret, ver)
	}
	sort.Strings(ret)
	return ret, nil
}
