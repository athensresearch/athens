package minio

import (
	"context"
	"fmt"

	"github.com/gomods/athens/pkg/storage"
	opentracing "github.com/opentracing/opentracing-go"
)

func (v *storageImpl) Delete(ctx context.Context, module, version string) error {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.minio.Delete")
	defer sp.Finish()
	if !v.Exists(ctx, module, version) {
		return storage.ErrVersionNotFound{
			Module:  module,
			Version: version,
		}
	}
	versionedPath := v.versionLocation(module, version)

	modPath := fmt.Sprintf("%s/go.mod", versionedPath)
	if err := v.minioClient.RemoveObject(v.bucketName, modPath); err != nil {
		return err
	}

	zipPath := fmt.Sprintf("%s/source.zip", versionedPath)
	if err := v.minioClient.RemoveObject(v.bucketName, zipPath); err != nil {
		return err
	}

	infoPath := fmt.Sprintf("%s/%s.info", versionedPath, version)
	return v.minioClient.RemoveObject(v.bucketName, infoPath)
}
