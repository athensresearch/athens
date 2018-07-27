package minio

import (
	"context"
	"fmt"

	minio "github.com/minio/minio-go"
	opentracing "github.com/opentracing/opentracing-go"
)

func (v *storageImpl) Exists(ctx context.Context, module, version string) bool {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.minio.Exists")
	defer sp.Finish()
	versionedPath := v.versionLocation(module, version)
	modPath := fmt.Sprintf("%s/go.mod", versionedPath)
	_, err := v.minioClient.StatObject(v.bucketName, modPath, minio.StatObjectOptions{})

	return err == nil
}
