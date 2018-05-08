package minio

import (
	"bytes"

	minio "github.com/minio/minio-go"
)

func (s *storageImpl) Save(module, vsn string, mod, zip []byte) error {
	dir := s.versionLocation(module, vsn)
	modFileName := dir + "/" + "go.mod"
	zipFileName := dir + "/" + "source.zip"
	_, err := s.minioClient.PutObject(s.bucketName, modFileName, bytes.NewReader(mod), int64(len(mod)), minio.PutObjectOptions{})
	if err != nil {
		return err
	}
	_, err = s.minioClient.PutObject(s.bucketName, zipFileName, bytes.NewReader(zip), int64(len(zip)), minio.PutObjectOptions{})
	if err != nil {
		return err
	}
	return nil
}
