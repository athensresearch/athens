package minio

import (
	"fmt"

	minio "github.com/minio/minio-go"
)

func (v *storageImpl) Exists(module, version string) bool {
	versionedPath := v.versionLocation(module, version)
	modPath := fmt.Sprintf("%s/go.mod", versionedPath)
	_, err := v.minioClient.StatObject(v.bucketName, modPath, minio.StatObjectOptions{})

	return err == nil
}
