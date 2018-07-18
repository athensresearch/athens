package minio

import (
	"testing"

	"github.com/gomods/athens/pkg/storage"
	minio "github.com/minio/minio-go"
	"github.com/stretchr/testify/suite"
)

type MinioTests struct {
	suite.Suite
	storage                                            storage.Backend
	endpoint, accessKeyID, secretAccessKey, bucketName string
}

func (d *MinioTests) SetupTest() {
	d.endpoint = "127.0.0.1:9000"
	d.bucketName = "gomods"
	d.accessKeyID = "minio"
	d.secretAccessKey = "minio123"
	storage, err := NewStorage(d.endpoint, d.accessKeyID, d.secretAccessKey, d.bucketName, false)
	d.Require().NoError(err)
	d.storage = storage
}

func (d *MinioTests) TearDownTest() {
	minioClient, _ := minio.New(d.endpoint, d.accessKeyID, d.secretAccessKey, false)
	doneCh := make(chan struct{})
	defer close(doneCh)
	objectCh := minioClient.ListObjectsV2(d.bucketName, "", true, doneCh)
	for object := range objectCh {
		d.Require().NoError(minioClient.RemoveObject(d.bucketName, object.Key))
	}
}

func TestMinioStorage(t *testing.T) {
	suite.Run(t, new(MinioTests))
}
