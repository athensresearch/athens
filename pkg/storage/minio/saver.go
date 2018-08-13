package minio

import (
	"bytes"
	"context"
	"io"

	"github.com/gomods/athens/pkg/errors"
	minio "github.com/minio/minio-go"
	opentracing "github.com/opentracing/opentracing-go"
)

func (s *storageImpl) Save(ctx context.Context, module, vsn string, mod []byte, zip io.Reader, info []byte) error {
	const op errors.Op = "minio.SAve"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.minio.Save")
	defer sp.Finish()
	dir := s.versionLocation(module, vsn)
	modFileName := dir + "/" + "go.mod"
	zipFileName := dir + "/" + "source.zip"
	infoFileName := dir + "/" + vsn + ".info"
	_, err := s.minioClient.PutObject(s.bucketName, modFileName, bytes.NewReader(mod), int64(len(mod)), minio.PutObjectOptions{})
	if err != nil {
		return errors.E(op, err)
	}
	_, err = s.minioClient.PutObject(s.bucketName, zipFileName, zip, -1, minio.PutObjectOptions{})
	if err != nil {
		return errors.E(op, err)
	}
	_, err = s.minioClient.PutObject(s.bucketName, infoFileName, bytes.NewReader(info), int64(len(info)), minio.PutObjectOptions{})
	if err != nil {
		return errors.E(op, err)
	}
	return nil
}
