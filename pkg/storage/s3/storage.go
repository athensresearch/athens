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
	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/errors"
	moduploader "github.com/gomods/athens/pkg/storage/module"
	"github.com/opentracing/opentracing-go"
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
	const op errors.Op = "s3.New"
	u, err := url.Parse(fmt.Sprintf("http://%s.s3.amazonaws.com", bucketName))
	if err != nil {
		return nil, errors.E(op, err)
	}

	// create a session
	sess, err := session.NewSession()
	if err != nil {
		return nil, errors.E(op, err)
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
	const op errors.Op = "s3.NewWithUploader"
	u, err := url.Parse(fmt.Sprintf("http://%s.s3.amazonaws.com", bucketName))
	if err != nil {
		return nil, errors.E(op, err)
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
	const op errors.Op = "s3.Save"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.s3.Save")
	defer sp.Finish()
	err := moduploader.Upload(ctx, module, version, bytes.NewReader(info), bytes.NewReader(mod), zip, s.upload)
	// TODO: take out lease on the /list file and add the version to it
	//
	// Do that only after module source+metadata is uploaded
	if err != nil {
		return errors.E(op, err)
	}
	return nil
}

func (s *Storage) upload(ctx context.Context, path, contentType string, stream io.Reader) error {
	const op errors.Op = "s3.upload"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.s3.upload")
	defer sp.Finish()
	upParams := &s3manager.UploadInput{
		Bucket:      &s.bucket,
		Key:         &path,
		Body:        stream,
		ContentType: &contentType,
	}
	_, err := s.uploader.UploadWithContext(ctx, upParams)
	if err != nil {
		return errors.E(op, err)
	}
	return nil
}
