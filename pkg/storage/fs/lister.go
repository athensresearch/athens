package fs

import (
	"context"
	"os"

	"github.com/gomods/athens/pkg/errors"
	opentracing "github.com/opentracing/opentracing-go"
	"github.com/spf13/afero"
)

func (l *storageImpl) List(ctx context.Context, module string) ([]string, error) {
	const op errors.Op = "fs.List"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.fs.List")
	defer sp.Finish()
	loc := l.moduleLocation(module)
	fileInfos, err := afero.ReadDir(l.filesystem, loc)
	if err != nil {
		kind := errors.KindUnexpected
		if os.IsNotExist(err) {
			kind = errors.KindNotFound
		}

		return nil, errors.E(op, errors.M(module), err, kind)
	}
	ret := []string{}
	for _, fileInfo := range fileInfos {
		if fileInfo.IsDir() {
			ret = append(ret, fileInfo.Name())
		}
	}
	return ret, nil
}
