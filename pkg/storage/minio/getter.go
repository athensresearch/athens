package minio

import (
	"fmt"
	"io/ioutil"
	"time"

	"github.com/gomods/athens/pkg/storage"
	minio "github.com/minio/minio-go"
)

func (v *storageImpl) Get(module, version string) (*storage.Version, error) {
	versionedPath := v.versionLocation(module, version)
	modPath := fmt.Sprintf("%s/go.mod", versionedPath)
	modReader, err := v.minioClient.GetObject(v.bucketName, modPath, minio.GetObjectOptions{})
	mod, err := ioutil.ReadAll(modReader)
	if err != nil {
		return nil, err
	}
	zipPath := fmt.Sprintf("%s/source.zip", versionedPath)
	zipReader, err := v.minioClient.GetObject(v.bucketName, zipPath, minio.GetObjectOptions{})
	if err != nil {
		return nil, err
	}

	// TODO: store the time in the saver, and parse it here
	return &storage.Version{
		RevInfo: storage.RevInfo{
			Version: version,
			Name:    version,
			Short:   version,
			Time:    time.Now(),
		},
		Mod: mod,
		Zip: zipReader,
	}, nil
}
