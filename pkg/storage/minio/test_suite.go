package minio

import (
	"github.com/gobuffalo/suite"
	"github.com/gomods/athens/pkg/storage"
	minio "github.com/minio/minio-go"
)

// TestSuite implements storage.TestSuite interface
type TestSuite struct {
	*suite.Model
	storage                                            storage.Backend
	endpoint, accessKeyID, secretAccessKey, bucketName string
}

// NewTestSuite creates a common test suite
func NewTestSuite(model *suite.Model) (storage.TestSuite, error) {
	endpoint := "127.0.0.1:9000"
	bucketName := "gomods"
	accessKeyID := "minio"
	secretAccessKey := "minio123"
	minioStorage, err := NewStorage(endpoint, accessKeyID, secretAccessKey, bucketName, false)

	return &TestSuite{
		storage:         minioStorage,
		Model:           model,
		endpoint:        endpoint,
		bucketName:      bucketName,
		accessKeyID:     accessKeyID,
		secretAccessKey: secretAccessKey,
	}, err
}

// Storage retrieves initialized storage backend
func (ts *TestSuite) Storage() storage.Backend {
	return ts.storage
}

// StorageHumanReadableName retrieves readable identifier of the storage
func (ts *TestSuite) StorageHumanReadableName() string {
	return "Minio"
}

// Cleanup tears down test
func (ts *TestSuite) Cleanup() {
	minioClient, _ := minio.New(ts.endpoint, ts.accessKeyID, ts.secretAccessKey, false)
	doneCh := make(chan struct{})
	defer close(doneCh)
	objectCh := minioClient.ListObjectsV2(ts.bucketName, "", true, doneCh)
	for object := range objectCh {
		ts.Require().NoError(minioClient.RemoveObject(ts.bucketName, object.Key))
	}
}
