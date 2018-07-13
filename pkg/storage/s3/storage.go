package s3

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"net/url"

	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3/s3manager"
	"github.com/aws/aws-sdk-go/service/s3/s3manager/s3manageriface"
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
	bucket   string
	baseURI  *url.URL
	uploader s3manageriface.UploaderAPI
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
	uploader := s3manager.NewUploader(sess)

	return &Storage{
		bucket:   bucketName,
		uploader: uploader,
		baseURI:  u,
	}, nil
}

// NewWithUploader creates a new AWS S3 CDN saver with provided uploader
func NewWithUploader(bucketName string, uploader s3manageriface.UploaderAPI) (*Storage, error) {
	u, err := url.Parse(fmt.Sprintf("http://%s.s3.amazonaws.com", bucketName))
	if err != nil {
		return nil, err
	}

	return &Storage{
		bucket:   bucketName,
		uploader: uploader,
		baseURI:  u,
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

	go s.upload(tctx, errChan, module, version, "mod", bytes.NewReader(mod))
	go s.upload(tctx, errChan, module, version, "zip", zip)
	go s.upload(tctx, errChan, module, version, "info", bytes.NewReader(info))

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

func (s *Storage) upload(ctx context.Context, errChan chan<- error, module, version, name string, content io.Reader) {
	save := func() error {
		key := config.PackageVersionedName(module, version, name)
		upParams := &s3manager.UploadInput{
			Bucket: &s.bucket,
			Key:    &key,
			Body:   content,
		}
		_, err := s.uploader.UploadWithContext(ctx, upParams)
		return err
	}

	select {
	case errChan <- save():
	case <-ctx.Done():
		errChan <- fmt.Errorf("uploading %s/%s.%s timed out", module, version, name)

	}
}
