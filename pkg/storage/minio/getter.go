package minio

import (
	"fmt"
	"io/ioutil"

	"github.com/gomods/athens/pkg/storage"
	minio "github.com/minio/minio-go"
)

func (v *storageImpl) Get(module, version string) (*storage.Version, error) {
	versionedPath := v.versionLocation(module, version)
	modPath := fmt.Sprintf("%s/go.mod", versionedPath)
	modReader, err := v.minioClient.GetObject(v.bucketName, modPath, minio.GetObjectOptions{})
	if err != nil {
		return nil, err
	}
	mod, err := ioutil.ReadAll(modReader)
	if err != nil {
		return nil, err
	}
	zipPath := fmt.Sprintf("%s/source.zip", versionedPath)
	zipReader, err := v.minioClient.GetObject(v.bucketName, zipPath, minio.GetObjectOptions{})
	if err != nil {
		return nil, err
	}
	infoPath := fmt.Sprintf("%s/%s.info", versionedPath, version)
	infoReader, err := v.minioClient.GetObject(v.bucketName, infoPath, minio.GetObjectOptions{})
	if err != nil {
		return nil, err
	}
	info, err := ioutil.ReadAll(infoReader)
	if err != nil {
		return nil, err
	}

	// TODO: store the time in the saver, and parse it here
	return &storage.Version{
		Mod:  mod,
		Zip:  zipReader,
		Info: info,
	}, nil
}
