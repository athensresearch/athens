package gcp

import (
	"context"
	"fmt"
	"net/url"

	"cloud.google.com/go/storage"
	"github.com/gomods/athens/pkg/config/env"
	"google.golang.org/api/option"
)

// Storage implements the (./pkg/storage).Backend interface
type Storage struct {
	bucket       Bucket
	baseURI      *url.URL
	closeStorage func() error
}

// NewWithCredentials returns a new Storage instance authenticated using the provided
// ClientOptions. The bucket name to be used will be loaded from the
// environment variable ATHENS_STORAGE_GCP_BUCKET.
//
// The ClientOptions should provide permissions sufficient to read, write and
// delete objects in google cloud storage for your project.
func NewWithCredentials(ctx context.Context, cred option.ClientOption) (*Storage, error) {
	storage, err := storage.NewClient(ctx, cred)
	if err != nil {
		return nil, fmt.Errorf("could not create new storage client: %s", err)
	}
	bucketname, err := env.GcpBucketName()
	if err != nil {
		return nil, err
	}
	u, err := url.Parse(fmt.Sprintf("https://storage.googleapis.com/%s", bucketname))
	if err != nil {
		return nil, err
	}
	bkt := gcpBucket{storage.Bucket(bucketname)}

	return &Storage{
		bucket:       &bkt,
		baseURI:      u,
		closeStorage: storage.Close,
	}, nil
}

// BaseURL returns the base URL that stores all modules. It can be used
// in the "meta" tag redirect response to vgo.
//
// For example:
//
//	<meta name="go-import" content="gomods.com/athens mod BaseURL()">
func (s *Storage) BaseURL() *url.URL {
	return env.CDNEndpointWithDefault(s.baseURI)
}

// Close calls the underlying storage client's close method
// It is not required to be called on program exit but provided here
// for completness.
func (s *Storage) Close() error {
	return s.closeStorage()
}
