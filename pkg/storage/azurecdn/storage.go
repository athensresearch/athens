package azurecdn

import (
	"bytes"
	"context"
	"fmt"
	"net/url"

	"github.com/Azure/azure-storage-blob-go/2017-07-29/azblob"
)

// Storage implements (github.com/gomods/athens/pkg/storage).Saver and
// also provides a function to fetch the location of a module
type Storage struct {
	accountURL *url.URL
	cred       azblob.Credential
}

// New creates a new azure CDN saver
func New(accountName, accountKey string) (*Storage, error) {
	u, err := url.Parse(fmt.Sprintf("https://%s.blob.core.windows.net", accountName))
	if err != nil {
		return nil, err
	}
	cred := azblob.NewSharedKeyCredential(accountName, accountKey)
	return &Storage{accountURL: u, cred: cred}, nil
}

// BaseURL returns the base URL that stores all modules. It can be used
// in the "meta" tag redirect response to vgo.
//
// For example:
//
//	<meta name="go-import" content="gomods.com/athens mod BaseURL()">
func (s Storage) BaseURL() *url.URL {
	return s.accountURL
}

// Save implements the (github.com/gomods/athens/pkg/storage).Saver interface.
func (s *Storage) Save(module, version string, mod, zip, info []byte) error {
	ctx := context.Background()

	pipe := azblob.NewPipeline(s.cred, azblob.PipelineOptions{})
	serviceURL := azblob.NewServiceURL(*s.accountURL, pipe)
	// rules on container names:
	// https://docs.microsoft.com/en-us/rest/api/storageservices/naming-and-referencing-containers--blobs--and-metadata#container-names
	//
	// This container must exist
	containerURL := serviceURL.NewContainerURL("gomodules")

	infoBlobURL := containerURL.NewBlockBlobURL(fmt.Sprintf("%s/@v/%s.info", module, version))
	modBlobURL := containerURL.NewBlockBlobURL(fmt.Sprintf("%s/@v/%s.mod", module, version))
	zipBlobURL := containerURL.NewBlockBlobURL(fmt.Sprintf("%s/@v/%s.zip", module, version))

	httpHeaders := func(contentType string) azblob.BlobHTTPHeaders {
		return azblob.BlobHTTPHeaders{
			ContentType: contentType,
		}
	}
	emptyMeta := map[string]string{}
	emptyBlobAccessCond := azblob.BlobAccessConditions{}
	// TODO: do these in parallel
	if _, err := infoBlobURL.Upload(ctx, bytes.NewReader(info), httpHeaders("application/json"), emptyMeta, emptyBlobAccessCond); err != nil {
		// TODO: log
		return err
	}
	if _, err := modBlobURL.Upload(ctx, bytes.NewReader(info), httpHeaders("text/plain"), emptyMeta, emptyBlobAccessCond); err != nil {
		// TODO: log
		return err
	}
	if _, err := zipBlobURL.Upload(ctx, bytes.NewReader(zip), httpHeaders("application/octet-stream"), emptyMeta, emptyBlobAccessCond); err != nil {
		// TODO: log
		return err
	}

	// TODO: take out lease on the /list file and add the version to it
	//
	// Do that only after module source+metadata is uploaded

	return nil
}
