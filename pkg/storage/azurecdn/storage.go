package azurecdn

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"net/url"

	"github.com/gomods/athens/pkg/config/env"
	"github.com/gomods/athens/pkg/errors"
	moduploader "github.com/gomods/athens/pkg/storage/module"
	"github.com/opentracing/opentracing-go"
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
	const op errors.Op = "azurecdn.New"
	u, err := url.Parse(fmt.Sprintf("https://%s.blob.core.windows.net", accountName))
	if err != nil {
		return nil, errors.E(op, err)
	}
	cl := newBlobStoreClient(u, accountName, accountKey, containerName)
	return &Storage{cl: cl, baseURI: u}, nil
}

// newWithClient creates a new azure CDN saver
func newWithClient(accountName, cl client) (*Storage, error) {
	const op errors.Op = "azurecdn.newWithClient"
	u, err := url.Parse(fmt.Sprintf("https://%s.blob.core.windows.net", accountName))
	if err != nil {
		return nil, errors.E(op, err)
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
	const op errors.Op = "azurecdn.Save"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.azurecdn.Save")
	sp.Finish()
	err := moduploader.Upload(ctx, module, version, bytes.NewReader(info), bytes.NewReader(mod), zip, s.cl.UploadWithContext)
	// TODO: take out lease on the /list file and add the version to it
	//
	// Do that only after module source+metadata is uploaded
	if err != nil {
		return errors.E(op, err)
	}
	return nil
}
