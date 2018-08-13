package azurecdn

import (
	"context"
	"io"
	"net/url"

	"github.com/Azure/azure-storage-blob-go/2017-07-29/azblob"
	"github.com/gomods/athens/pkg/errors"
	"github.com/opentracing/opentracing-go"
)

type azureBlobStoreClient struct {
	containerURL *azblob.ContainerURL
}

func newBlobStoreClient(accountURL *url.URL, accountName, accountKey, containerName string) *azureBlobStoreClient {
	cred := azblob.NewSharedKeyCredential(accountName, accountKey)
	pipe := azblob.NewPipeline(cred, azblob.PipelineOptions{})
	serviceURL := azblob.NewServiceURL(*accountURL, pipe)
	// rules on container names:
	// https://docs.microsoft.com/en-us/rest/api/storageservices/naming-and-referencing-containers--blobs--and-metadata#container-names
	//
	// This container must exist
	containerURL := serviceURL.NewContainerURL(containerName)
	cl := &azureBlobStoreClient{containerURL: &containerURL}
	return cl
}

func (c *azureBlobStoreClient) UploadWithContext(ctx context.Context, path, contentType string, content io.Reader) error {
	const op errors.Op = "azurecdn.UploadWithContext"
	sp, ctx := opentracing.StartSpanFromContext(ctx, "storage.azurecdn.UploadWithContext")
	defer sp.Finish()
	blobURL := c.containerURL.NewBlockBlobURL(path)
	emptyMeta := map[string]string{}
	emptyBlobAccessCond := azblob.BlobAccessConditions{}
	httpHeaders := func(contentType string) azblob.BlobHTTPHeaders {
		return azblob.BlobHTTPHeaders{
			ContentType: contentType,
		}
	}
	bufferSize := 1 * 1024 * 1024 // Size of the rotating buffers that are used when uploading
	maxBuffers := 3               // Number of rotating buffers that are used when uploading

	uploadStreamOpts := azblob.UploadStreamToBlockBlobOptions{
		BufferSize:       bufferSize,
		MaxBuffers:       maxBuffers,
		BlobHTTPHeaders:  httpHeaders(contentType),
		Metadata:         emptyMeta,
		AccessConditions: emptyBlobAccessCond,
	}
	_, err := azblob.UploadStreamToBlockBlob(ctx, content, blobURL, uploadStreamOpts)
	if err != nil {
		return errors.E(op, err)
	}
	return nil
}
