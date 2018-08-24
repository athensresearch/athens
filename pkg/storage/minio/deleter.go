package minio

import (
	"context"
	"fmt"

	"github.com/gomods/athens/pkg/errors"
	opentracing "github.com/opentracing/opentracing-go"
)

func (v *storageImpl) Delete(ctx context.Context, module, version string) error {
	const op errors.Op = "minio.Delete"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.minio.Delete")
	defer sp.Finish()
	exists, err := v.Exists(ctx, module, version)
	if err != nil {
		return errors.E(op, err, errors.M(module), errors.V(version))
	}

	if !exists {
		return errors.E(op, errors.M(module), errors.V(version), errors.KindNotFound)
	}

	versionedPath := v.versionLocation(module, version)

	modPath := fmt.Sprintf("%s/go.mod", versionedPath)
	if err := v.minioClient.RemoveObject(v.bucketName, modPath); err != nil {
		return errors.E(op, err, errors.M(module), errors.V(version))
	}

	zipPath := fmt.Sprintf("%s/source.zip", versionedPath)
	if err := v.minioClient.RemoveObject(v.bucketName, zipPath); err != nil {
		return errors.E(op, err, errors.M(module), errors.V(version))
	}

	infoPath := fmt.Sprintf("%s/%s.info", versionedPath, version)
	err = v.minioClient.RemoveObject(v.bucketName, infoPath)
	if err != nil {
		return errors.E(op, err, errors.M(module), errors.V(version))
	}
	return nil
}
