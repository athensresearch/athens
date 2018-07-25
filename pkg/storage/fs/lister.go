package fs

import (
	"context"

	opentracing "github.com/opentracing/opentracing-go"
	"github.com/spf13/afero"
)

func (l *storageImpl) List(ctx context.Context, module string) ([]string, error) {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.fs.List")
	defer sp.Finish()
	loc := l.moduleLocation(module)
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
