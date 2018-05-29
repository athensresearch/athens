package minio

import (
	"testing"

	"github.com/gomods/athens/pkg/storage"
	minio "github.com/minio/minio-go"
	"github.com/stretchr/testify/suite"
)

const (
	module  = "testmodule"
	version = "v1.0.0"
)

var (
	// TODO: put these values inside of the suite, and generate longer values.
	// This should help catch edge cases, like https://github.com/gomods/athens/issues/38
	//
	// Also, consider doing something similar to what testing/quick does
	// with the Generator interface (https://godoc.org/testing/quick#Generator).
	// The rough, simplified idea would be to run a single test case multiple
	// times over different (increasing) values.
	mod  = []byte("123")
	zip  = []byte("456")
	info = []byte("789")
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
	minioStorage, err := NewStorage(d.endpoint, d.accessKeyID, d.secretAccessKey, d.bucketName, false)
	d.Require().NoError(err)
	d.storage = minioStorage
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
