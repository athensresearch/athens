package minio

import (
	"context"
	"fmt"

	"github.com/gomods/athens/pkg/errors"
	minio "github.com/minio/minio-go"
	opentracing "github.com/opentracing/opentracing-go"
)

const (
	minioErrorCodeNoSuchKey           = "NoSuchKey"
	op                      errors.Op = "storage.minio.Exists"
)

func (v *storageImpl) Exists(ctx context.Context, module, version string) (bool, error) {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.minio.Exists")
	defer sp.Finish()
	versionedPath := v.versionLocation(module, version)
	modPath := fmt.Sprintf("%s/go.mod", versionedPath)
	_, err := v.minioClient.StatObject(v.bucketName, modPath, minio.StatObjectOptions{})

	if minio.ToErrorResponse(err).Code == minioErrorCodeNoSuchKey {
		return false, nil
	}

	if err != nil {
		return false, errors.E(op, err)
	}

	return true, nil
}
