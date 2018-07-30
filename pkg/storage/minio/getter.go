package minio

import (
	"context"
	"fmt"
	"io/ioutil"
	"net/http"

	"github.com/gomods/athens/pkg/storage"
	minio "github.com/minio/minio-go"
	opentracing "github.com/opentracing/opentracing-go"
)

func (v *storageImpl) Get(ctx context.Context, module, version string) (*storage.Version, error) {
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.minio.Get")
	defer sp.Finish()
	versionedPath := v.versionLocation(module, version)
	modPath := fmt.Sprintf("%s/go.mod", versionedPath)
	modReader, err := v.minioClient.GetObject(v.bucketName, modPath, minio.GetObjectOptions{})
	if err != nil {
		return nil, err
	}
	mod, err := ioutil.ReadAll(modReader)
	if err != nil {
		return nil, transformNotFoundErr(module, version, err)
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
		return nil, transformNotFoundErr(module, version, err)
	}

	// TODO: store the time in the saver, and parse it here
	return &storage.Version{
		Mod:  mod,
		Zip:  zipReader,
		Info: info,
	}, nil
}

func transformNotFoundErr(module, version string, err error) error {
	if eresp, ok := err.(minio.ErrorResponse); ok {
		if eresp.StatusCode == http.StatusNotFound {
			return storage.ErrVersionNotFound{Module: module, Version: version}
		}
	}
	return err
}
