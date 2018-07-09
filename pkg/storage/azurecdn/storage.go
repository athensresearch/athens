package azurecdn

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"net/url"

	"github.com/Azure/azure-storage-blob-go/2017-07-29/azblob"
	"github.com/gomods/athens/pkg/config"
	"github.com/gomods/athens/pkg/config/env"
	multierror "github.com/hashicorp/go-multierror"
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
	return env.CDNEndpointWithDefault(s.accountURL)
}

// Save implements the (github.com/gomods/athens/pkg/storage).Saver interface.
func (s *Storage) Save(ctx context.Context, module, version string, mod []byte, zip io.Reader, info []byte) error {
	tctx, cancel := context.WithTimeout(ctx, env.Timeout())
	defer cancel()

	pipe := azblob.NewPipeline(s.cred, azblob.PipelineOptions{})
	serviceURL := azblob.NewServiceURL(*s.accountURL, pipe)
	// rules on container names:
	// https://docs.microsoft.com/en-us/rest/api/storageservices/naming-and-referencing-containers--blobs--and-metadata#container-names
	//
	// This container must exist
	containerURL := serviceURL.NewContainerURL("gomodules")

	emptyMeta := map[string]string{}
	emptyBlobAccessCond := azblob.BlobAccessConditions{}
	httpHeaders := func(contentType string) azblob.BlobHTTPHeaders {
		return azblob.BlobHTTPHeaders{
			ContentType: contentType,
		}
	}
	uploadOpts := func(contentType string) uploadOptions {
		return uploadOptions{
			accessConditions: emptyBlobAccessCond,
			metadata:         emptyMeta,
			blobHTTPHeaders:  httpHeaders(contentType),
		}
	}

	const numUpload = 3
	errChan := make(chan error, numUpload)

	go uploadBlob(tctx, errChan, containerURL, module, version, "info", bytes.NewReader(info), uploadOpts("application/json"))
	go uploadBlob(tctx, errChan, containerURL, module, version, "mod", bytes.NewReader(mod), uploadOpts("text/plain"))
	go uploadStream(tctx, errChan, containerURL, module, version, "zip", zip, uploadOpts("application/octet-stream"))

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

func blockBlobURL(containerURL azblob.ContainerURL, module, version, ext string) azblob.BlockBlobURL {
	return containerURL.NewBlockBlobURL(config.PackageVersionedName(module, version, ext))
}

func uploadBlob(ctx context.Context, errChan chan<- error, containerURL azblob.ContainerURL, module, version, ext string, content io.ReadSeeker, opts uploadOptions) {
	url := blockBlobURL(containerURL, module, version, ext)
	save := func() error {
		_, err := url.Upload(ctx, content, opts.blobHTTPHeaders, opts.metadata, opts.accessConditions)
		return err
	}

	select {
	case errChan <- save():
	case <-ctx.Done():
		errChan <- fmt.Errorf("uploading %s/%s.%s timed out", module, version, ext)
	}
}

func uploadStream(ctx context.Context, errChan chan<- error, containerURL azblob.ContainerURL, module, version, ext string, stream io.Reader, opts uploadOptions) {
	bufferSize := 1 * 1024 * 1024 // Size of the rotating buffers that are used when uploading
	maxBuffers := 3               // Number of rotating buffers that are used when uploading

	uploadStreamOpts := azblob.UploadStreamToBlockBlobOptions{
		BufferSize:       bufferSize,
		MaxBuffers:       maxBuffers,
		BlobHTTPHeaders:  opts.blobHTTPHeaders,
		Metadata:         opts.metadata,
		AccessConditions: opts.accessConditions,
	}
	url := blockBlobURL(containerURL, module, version, ext)
	save := func() error {
		_, err := azblob.UploadStreamToBlockBlob(ctx, stream, url, uploadStreamOpts)
		return err
	}

	select {
	case errChan <- save():
	case <-ctx.Done():
		errChan <- fmt.Errorf("uploading %s/%s.%s timed out", module, version, ext)
	}
}

type uploadOptions struct {
	blobHTTPHeaders  azblob.BlobHTTPHeaders
	metadata         map[string]string
	accessConditions azblob.BlobAccessConditions
}
