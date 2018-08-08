package fs

import (
	"context"
	"io"
	"os"
	"path/filepath"

	"github.com/gomods/athens/pkg/errors"
	"github.com/opentracing/opentracing-go"
	"github.com/spf13/afero"
)

func (v *storageImpl) Info(ctx context.Context, module, version string) ([]byte, error) {
	const op errors.Op = "fs.Info"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.fs.Info")
	defer sp.Finish()
	versionedPath := v.versionLocation(module, version)
	info, err := afero.ReadFile(v.filesystem, filepath.Join(versionedPath, version+".info"))
	if err != nil {
		return nil, errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
	}

	return info, nil
}

func (v *storageImpl) GoMod(ctx context.Context, module, version string) ([]byte, error) {
	const op errors.Op = "fs.GoMod"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.fs.GoMod")
	defer sp.Finish()
	versionedPath := v.versionLocation(module, version)
	mod, err := afero.ReadFile(v.filesystem, filepath.Join(versionedPath, "go.mod"))
	if err != nil {
		return nil, errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
	}

	return mod, nil
}

func (v *storageImpl) Zip(ctx context.Context, module, version string) (io.ReadCloser, error) {
	const op errors.Op = "fs.Zip"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.fs.Zip")
	defer sp.Finish()
	versionedPath := v.versionLocation(module, version)

	src, err := v.filesystem.OpenFile(filepath.Join(versionedPath, "source.zip"), os.O_RDONLY, 0666)
	if err != nil {
		return nil, errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
	}

	return src, nil
}
