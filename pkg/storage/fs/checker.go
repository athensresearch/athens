package fs

import (
	"context"
	"path/filepath"

	"github.com/gomods/athens/pkg/errors"
	opentracing "github.com/opentracing/opentracing-go"
	"github.com/spf13/afero"
)

func (v *storageImpl) Exists(ctx context.Context, module, version string) (bool, error) {
	const op errors.Op = "fs.Exists"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.fs.Exists")
	defer sp.Finish()
	versionedPath := v.versionLocation(module, version)
	exists, err := afero.Exists(v.filesystem, filepath.Join(versionedPath, "go.mod"))
	if err != nil {
		return false, errors.E(op, err)
	}

	return exists, nil
}
