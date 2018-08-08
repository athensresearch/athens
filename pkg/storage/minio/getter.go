package minio

import (
	"context"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"

	"github.com/gomods/athens/pkg/errors"
	minio "github.com/minio/minio-go"
	opentracing "github.com/opentracing/opentracing-go"
)

func (v *storageImpl) Info(ctx context.Context, module, vsn string) ([]byte, error) {
	const op errors.Op = "minio.Info"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.minio.Info")
	defer sp.Finish()
	infoPath := fmt.Sprintf("%s/%s.info", v.versionLocation(module, vsn), vsn)
	infoReader, err := v.minioClient.GetObject(v.bucketName, infoPath, minio.GetObjectOptions{})
	if err != nil {
		return nil, errors.E(op, err)
	}
	info, err := ioutil.ReadAll(infoReader)
	if err != nil {
		return nil, transformNotFoundErr(op, module, vsn, err)
	}

	return info, nil
}

func (v *storageImpl) GoMod(ctx context.Context, module, vsn string) ([]byte, error) {
	const op errors.Op = "minio.GetMod"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.minio.GoMod")
	defer sp.Finish()
	modPath := fmt.Sprintf("%s/go.mod", v.versionLocation(module, vsn))
	modReader, err := v.minioClient.GetObject(v.bucketName, modPath, minio.GetObjectOptions{})
	if err != nil {
		return nil, errors.E(op, err)
	}
	mod, err := ioutil.ReadAll(modReader)
	if err != nil {
		return nil, transformNotFoundErr(op, module, vsn, err)
	}

	return mod, nil
}
func (v *storageImpl) Zip(ctx context.Context, module, vsn string) (io.ReadCloser, error) {
	const op errors.Op = "minio.Zip"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.minio.Zip")
	defer sp.Finish()
	zipPath := fmt.Sprintf("%s/source.zip", v.versionLocation(module, vsn))
	zipReader, err := v.minioClient.GetObject(v.bucketName, zipPath, minio.GetObjectOptions{})
	if err != nil {
		return nil, errors.E(op, err)
	}

	return zipReader, nil
}

func transformNotFoundErr(op errors.Op, module, version string, err error) error {
	if eresp, ok := err.(minio.ErrorResponse); ok {
		if eresp.StatusCode == http.StatusNotFound {
			return errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
		}
	}
	return err
}
