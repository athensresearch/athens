package azurecdn

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"net/url"

	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/config/env"
	multierror "github.com/hashicorp/go-multierror"
)

type client interface {
	UploadWithContext(ctx context.Context, path, contentType string, content io.Reader) error
}

// Storage implements (github.com/gomods/athens/pkg/storage).Saver and
// also provides a function to fetch the location of a module
type Storage struct {
	cl      client
	baseURI *url.URL
}

// New creates a new azure CDN saver
func New(accountName, accountKey, containerName string) (*Storage, error) {
	u, err := url.Parse(fmt.Sprintf("https://%s.blob.core.windows.net", accountName))
	if err != nil {
		return nil, err
	}
	cl, err := newBlobStoreClient(u, accountName, accountKey, containerName)
	if err != nil {
		return nil, err
	}
	return &Storage{cl: cl, baseURI: u}, nil
}

// newWithClient creates a new azure CDN saver
func newWithClient(accountName, cl client) (*Storage, error) {
	u, err := url.Parse(fmt.Sprintf("https://%s.blob.core.windows.net", accountName))
	if err != nil {
		return nil, err
	}
	return &Storage{cl: cl, baseURI: u}, nil
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
	tctx, cancel := context.WithTimeout(ctx, env.Timeout())
	defer cancel()

	const numUpload = 3
	errChan := make(chan error, numUpload)

	go s.uploadStream(tctx, errChan, module, version, "info", bytes.NewReader(info), "application/json")
	go s.uploadStream(tctx, errChan, module, version, "mod", bytes.NewReader(mod), "text/plain")
	go s.uploadStream(tctx, errChan, module, version, "zip", zip, "application/octet-stream")

	var errors error
	for i := 0; i < numUpload; i++ {
		err := <-errChan
		if err != nil {
			errors = multierror.Append(errors, err)
		}
	}
	close(errChan)
	// TODO: take out lease on the /list file and add the version to it
	//
	// Do that only after module source+metadata is uploaded
	return errors
}

func (s *Storage) uploadStream(ctx context.Context, errChan chan<- error, module, version, ext string, stream io.Reader, contentType string) {
	select {
	case errChan <- s.cl.UploadWithContext(ctx, config.PackageVersionedName(module, version, ext), contentType, stream):
	case <-ctx.Done():
		errChan <- fmt.Errorf("uploading %s/%s.%s timed out", module, version, ext)
	}
}
