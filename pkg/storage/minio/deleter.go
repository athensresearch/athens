package minio

import (
	"fmt"
)

func (v *storageImpl) Delete(module, version string) error {
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
