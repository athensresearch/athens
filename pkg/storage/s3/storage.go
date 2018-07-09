package s3

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"io/ioutil"
	"net/url"

	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	"github.com/aws/aws-sdk-go/service/s3/s3iface"
	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/config/env"
	multierror "github.com/hashicorp/go-multierror"
)

// Storage implements (github.com/gomods/athens/pkg/storage).Saver and
// also provides a function to fetch the location of a module
// Storage uses amazon aws go SDK which expects these env variables
// - AWS_REGION 			- region for this storage, e.g 'us-west-2'
// - AWS_ACCESS_KEY_ID		-
// - AWS_SECRET_ACCESS_KEY 	-
// - AWS_SESSION_TOKEN		- [optional]
// For information how to get your keyId and access key turn to official aws docs: https://docs.aws.amazon.com/sdk-for-go/v1/developer-guide/setting-up.html
type Storage struct {
	bucket  string
	client  s3iface.S3API
	baseURI *url.URL
}

// New creates a new AWS S3 CDN saver
func New(bucketName string) (*Storage, error) {
	u, err := url.Parse(fmt.Sprintf("http://%s.s3.amazonaws.com", bucketName))
	if err != nil {
		return nil, err
	}

	// create a session
	sess, err := session.NewSession()
	if err != nil {
		return nil, err
	}

	// client with session
	client := s3.New(sess)
	return &Storage{
		bucket:  bucketName,
		client:  client,
		baseURI: u,
	}, nil
}

// NewWithClient creates a new AWS S3 CDN saver with provided client
func NewWithClient(bucketName string, client s3iface.S3API) (*Storage, error) {
	u, err := url.Parse(fmt.Sprintf("http://%s.s3.amazonaws.com", bucketName))
	if err != nil {
		return nil, err
	}

	return &Storage{
		bucket:  bucketName,
		client:  client,
		baseURI: u,
	}, nil
}

// BaseURL returns the base URL that stores all modules. It can be used
// in the "meta" tag redirect response to vgo.
//
// For example:
//
//	<meta name="go-import" content="gomods.com/athens mod BaseURL()">
func (s Storage) BaseURL() *url.URL {
	return env.CDNEndpointWithDefault(s.baseURI)
}

// Save implements the (github.com/gomods/athens/pkg/storage).Saver interface.
func (s *Storage) Save(ctx context.Context, module, version string, mod []byte, zip io.Reader, info []byte) error {
	errChan := make(chan error, 3)

	tctx, cancel := context.WithTimeout(ctx, env.Timeout())
	defer cancel()

	zipBytes, err := ioutil.ReadAll(zip)
	if err != nil {
		return err
	}

	go s.upload(tctx, errChan, module, version, "mod", mod)
	go s.upload(tctx, errChan, module, version, "zip", zipBytes)
	go s.upload(tctx, errChan, module, version, "info", info)

	var errors error
	for i := 0; i < 3; i++ {
		err := <-errChan
		if err != nil {
			errors = multierror.Append(errors, err)
		}
	}
	close(errChan)

	return errors
}

func (s *Storage) upload(ctx context.Context, errChan chan<- error, module, version, name string, content []byte) {
	key := config.PackageVersionedName(module, version, name)

	save := func() error {
		_, err := s.client.PutObjectWithContext(ctx, &s3.PutObjectInput{
			Bucket: &s.bucket,
			Key:    &key,
			Body:   bytes.NewReader(content),
		})
		return err
	}

	select {
	case errChan <- save():
	case <-ctx.Done():
		errChan <- fmt.Errorf("uploading %s/%s.%s timed out", module, version, name)

	}
}
